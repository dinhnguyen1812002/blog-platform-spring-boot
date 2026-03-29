package com.Nguyen.blogplatform.service.comment;

import com.Nguyen.blogplatform.exception.CommentDepthException;
import com.Nguyen.blogplatform.exception.ForbiddenException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Comment;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.CommentRequest;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.repository.CommentRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int MAX_REPLY_DEPTH       = 3;
    private static final int MAX_REPLIES_PER_LEVEL = 5;

    private final CommentRepository   commentRepository;
    private final PostRepository      postRepository;
    private final UserRepository      userRepository;
    private final NotificationService notificationService;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "comments", allEntries = true),
            @CacheEvict(value = "replies", key = "#request.parentCommentId",
                    condition = "#request.parentCommentId != null")
    })
    public CommentResponse createComment(String postId, CommentRequest request, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Comment comment = buildComment(request, post, user);
        Comment saved   = commentRepository.save(comment);
        CommentResponse response = convertToResponse(saved);

        dispatchNotifications(post, user, postId, response);
        return response;
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Only the comment owner may edit content.
     * Admins use deleteComment; editing someone else's words is not permitted even for admins.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "comments", allEntries = true),
            @CacheEvict(value = "replies",  allEntries = true)
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
     * Admin (isAdmin = true, resolved in controller via @PreAuthorize) can delete any comment.
     *
     * Deleting a parent comment also removes its entire reply subtree (cascade DELETE
     * should be configured on Comment.replies; if not, use commentRepository.deleteById
     * with CascadeType.REMOVE or handle it explicitly here).
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "comments", allEntries = true),
            @CacheEvict(value = "replies",  allEntries = true)
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
    @Cacheable(value = "comments",
            key   = "#postId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<CommentResponse> getTopLevelComments(String postId, Pageable pageable) {
        PageRequest pr = toSortedPageRequest(pageable, "createdAt", Sort.Direction.DESC);
        return commentRepository
                .findByPostIdAndParentCommentIsNull(postId, pr)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "replies",
            key   = "#commentId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<CommentResponse> getReplies(String commentId, Pageable pageable) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment not found: " + commentId);
        }
        PageRequest pr = toSortedPageRequest(pageable, "createdAt", Sort.Direction.DESC);
        return commentRepository
                .findByParentCommentId(commentId, pr)
                .map(this::convertToResponse);
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

    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setDepth(comment.getDepth());
        response.setReplyCount(comment.getReplyCount());

        if (comment.getUser() != null) {
            response.setUser(toUserResponse(comment.getUser()));
        }
        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }
        if (comment.getDepth() < MAX_REPLY_DEPTH && comment.getReplyCount() > 0
                && !comment.getReplies().isEmpty()) {

            List<CommentResponse> replies = comment.getReplies().stream()
                    .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                    .limit(MAX_REPLIES_PER_LEVEL)
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            response.setReplies(replies);
            response.setHasMoreReplies(comment.getReplyCount() > MAX_REPLIES_PER_LEVEL);
        }
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
    // Private — async notifications
    // -------------------------------------------------------------------------

    private void dispatchNotifications(Post post, User commenter,
                                       String postId, CommentResponse response) {
        if (!post.getAuthor().getId().equals(commenter.getId())) {
            sendAuthorNotification(post.getAuthor().getId(),
                    commenter.getUsername(), post.getTitle(), postId);
        }
        sendRealtimeNotifications(postId, response, commenter.getUsername(), post.getTitle());
    }

    @Async
    protected void sendAuthorNotification(String authorId, String commenterName,
                                          String postTitle, String postId) {
        try {
            notificationService.notifyPostAuthorAboutComment(
                    authorId, commenterName, postTitle, postId);
        } catch (Exception e) {
            log.warn("Failed to notify post author '{}': {}", authorId, e.getMessage());
        }
    }

    @Async
    protected void sendRealtimeNotifications(String postId, CommentResponse response,
                                             String commenterName, String postTitle) {
        try {
            notificationService.sendCommentNotification(postId, response);
            notificationService.sendGlobalNotification(
                    "New comment from: " + commenterName + " on post: " + postTitle);
        } catch (Exception e) {
            log.warn("Failed to send real-time notification: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private — utilities
    // -------------------------------------------------------------------------

    private PageRequest toSortedPageRequest(Pageable pageable,
                                            String field, Sort.Direction direction) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(direction, field));
    }
}