package com.Nguyen.blogplatform.domain.comment.application;

import com.Nguyen.blogplatform.domain.comment.domain.model.Comment;
import com.Nguyen.blogplatform.domain.comment.dto.CommentRequest;
import com.Nguyen.blogplatform.domain.comment.dto.CommentResponse;
import com.Nguyen.blogplatform.domain.comment.infrastructure.repository.CommentRepository;
import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.dto.UserResponse;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import com.Nguyen.blogplatform.shared.exception.CommentDepthException;
import com.Nguyen.blogplatform.shared.exception.ForbiddenException;
import com.Nguyen.blogplatform.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int MAX_REPLY_DEPTH = 3;
    private static final int MAX_REPLIES_PER_LEVEL = 5;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentNotificationDispatcher notificationDispatcher;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "comments", allEntries = true),
            @CacheEvict(value = "replies", allEntries = true)
    })
    public CommentResponse createComment(String postId, CommentRequest request, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Comment comment = buildComment(request, post, user);
        Comment saved = commentRepository.save(comment);
        CommentResponse response = convertToResponse(saved);

        String authorId = post.getAuthor().getId();
        String commenterId = user.getId();
        String commenterName = user.getUsername();
        String postTitle = post.getTitle();
        TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        notificationDispatcher.dispatch(authorId, commenterId, commenterName, postTitle, postId,
                                response);
                    }
                });
        return response;
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Only the comment owner may edit content.
     * Admins use deleteComment; editing someone else's words is not permitted even
     * for admins.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "comments", allEntries = true),
            @CacheEvict(value = "replies", allEntries = true)
    })
    public CommentResponse updateComment(String commentId, CommentRequest request, String userId) {
        Comment comment = findCommentById(commentId);
        assertOwner(comment, userId, "edit");

        comment.setContent(request.getContent());
        Comment saved = commentRepository.save(comment);

        log.debug("Comment '{}' updated by user '{}'", commentId, userId);
        return convertToResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    /**
     * Owner can delete their own comment.
     * Admin (isAdmin = true, resolved in controller via @PreAuthorize) can delete
     * any comment.
     *
     * Deleting a parent comment also removes its entire reply subtree (cascade
     * DELETE
     * should be configured on Comment.replies; if not, use
     * commentRepository.deleteById
     * with CascadeType.REMOVE or handle it explicitly here).
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "comments", allEntries = true),
            @CacheEvict(value = "replies", allEntries = true)
    })
    public void deleteComment(String commentId, String userId, boolean isAdmin) {
        Comment comment = findCommentById(commentId);

        if (!isAdmin) {
            assertOwner(comment, userId, "delete");
        }

        // Keep parent's replyCount in sync
        if (comment.getParentComment() != null) {
            Comment parent = comment.getParentComment();
            parent.removeReply(comment);
            commentRepository.save(parent);
        }

        commentRepository.delete(comment);
        log.info("Comment '{}' deleted by user '{}' (admin={})", commentId, userId, isAdmin);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#postId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<CommentResponse> getTopLevelComments(String postId, Pageable pageable) {
        PageRequest pr = toSortedPageRequest(pageable, "createdAt", Sort.Direction.DESC);
        Page<Comment> page = commentRepository.findByPostIdAndParentCommentIsNull(postId, pr);
        Map<String, CommentResponse> responses = mapCommentTrees(page.getContent());
        return page.map(comment -> responses.get(comment.getId()));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "replies", key = "#commentId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<CommentResponse> getReplies(String commentId, Pageable pageable) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment not found: " + commentId);
        }
        PageRequest pr = toSortedPageRequest(pageable, "createdAt", Sort.Direction.DESC);
        Page<Comment> page = commentRepository.findByParentCommentId(commentId, pr);
        Map<String, CommentResponse> responses = mapCommentTrees(page.getContent());
        return page.map(comment -> responses.get(comment.getId()));
    }

    // -------------------------------------------------------------------------
    // Private — build helpers
    // -------------------------------------------------------------------------

    private Comment buildComment(CommentRequest request, Post post, User user) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setUser(user);
        comment.setDepth(0);

        if (request.getParentCommentId() != null) {
            attachToParent(comment, request.getParentCommentId());
        }
        return comment;
    }

    private void attachToParent(Comment comment, String parentId) {
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent comment not found: " + parentId));

        if (parent.getDepth() >= MAX_REPLY_DEPTH) {
            throw new CommentDepthException(
                    "Maximum reply depth of " + MAX_REPLY_DEPTH + " reached");
        }

        comment.setParentComment(parent);
        comment.setDepth(parent.getDepth() + 1);
        parent.addReply(comment);
    }

    // -------------------------------------------------------------------------
    // Private — guard helpers
    // -------------------------------------------------------------------------

    private Comment findCommentById(String id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + id));
    }

    /** Throws HTTP 403 if the requesting user is not the comment owner. */
    private void assertOwner(Comment comment, String userId, String action) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to " + action + " this comment.");
        }
    }

    // -------------------------------------------------------------------------
    // Private — mapping
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Map<String, List<CommentResponse>> getFirstCommentsForPosts(List<String> postIds, int limit) {
        if (postIds.isEmpty())
            return Map.of();
        List<String> ids = commentRepository.findLatestTopLevelIds(postIds, limit);
        if (ids.isEmpty())
            return Map.of();
        List<Comment> roots = commentRepository.findByIdIn(ids);
        Map<String, CommentResponse> responses = mapCommentTrees(roots);
        Map<String, List<CommentResponse>> result = new HashMap<>();
        roots.stream().sorted(Comparator.comparing(Comment::getCreatedAt).reversed()
                .thenComparing(Comment::getId, Comparator.reverseOrder()))
                .forEach(c -> result.computeIfAbsent(c.getPost().getId(), ignored -> new ArrayList<>())
                        .add(responses.get(c.getId())));
        return result;
    }

    private Map<String, CommentResponse> mapCommentTrees(List<Comment> roots) {
        Map<String, CommentResponse> result = new HashMap<>();
        List<Comment> current = roots;
        while (!current.isEmpty()) {
            for (Comment comment : current)
                result.computeIfAbsent(comment.getId(), ignored -> mapComment(comment));
            List<String> parents = current.stream()
                    .filter(c -> c.getDepth() < MAX_REPLY_DEPTH && c.getReplyCount() > 0)
                    .map(Comment::getId).toList();
            if (parents.isEmpty())
                break;
            List<String> childIds = commentRepository.findLatestReplyIds(parents, MAX_REPLIES_PER_LEVEL);
            if (childIds.isEmpty())
                break;
            List<Comment> children = new ArrayList<>(commentRepository.findByIdIn(childIds));
            children.sort(Comparator.comparing(Comment::getCreatedAt).reversed()
                    .thenComparing(Comment::getId, Comparator.reverseOrder()));
            for (Comment child : children) {
                CommentResponse parent = result.get(child.getParentComment().getId());
                if (parent.getReplies() == null)
                    parent.setReplies(new ArrayList<>());
                CommentResponse childResponse = mapComment(child);
                parent.getReplies().add(childResponse);
                result.put(child.getId(), childResponse);
            }
            // Preserve the same response objects when the next level is populated.
            current = children;
        }
        return result;
    }

    private CommentResponse convertToResponse(Comment comment) {
        return mapCommentTrees(List.of(comment)).get(comment.getId());
    }

    private CommentResponse mapComment(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setDepth(comment.getDepth());
        response.setReplyCount(comment.getReplyCount());
        if (comment.getUser() != null)
            response.setUser(toUserResponse(comment.getUser()));
        if (comment.getParentComment() != null)
            response.setParentCommentId(comment.getParentComment().getId());
        response.setHasMoreReplies(
                comment.getDepth() < MAX_REPLY_DEPTH && comment.getReplyCount() > MAX_REPLIES_PER_LEVEL);
        return response;
    }

    private UserResponse toUserResponse(User user) {
        UserResponse ur = new UserResponse();
        ur.setId(user.getId());
        ur.setUsername(user.getUsername());
        ur.setEmail(user.getEmail());
        ur.setAvatar(user.getAvatar());
        return ur;
    }

    // -------------------------------------------------------------------------
    // Private — utilities
    // -------------------------------------------------------------------------

    private PageRequest toSortedPageRequest(Pageable pageable,
            String field, Sort.Direction direction) {
        return PageRequest.of(pageable.getPageNumber(),
                com.Nguyen.blogplatform.shared.util.PageLimits.size(pageable.getPageSize()),
                Sort.by(direction, field));
    }
}