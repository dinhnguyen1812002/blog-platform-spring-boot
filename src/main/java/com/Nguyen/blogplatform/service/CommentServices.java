package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.CommentDepthException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Comment;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.CommentRequest;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.repository.CommentRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServices {
    private static final int MAX_REPLY_DEPTH = 3;
    private static final int MAX_REPLIES_PER_LEVEL = 5;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper  modelMapper;
    private final NotificationService notificationService;


    @Transactional
    @CacheEvict(value = "comments", key = "#postId")
    public CommentResponse createComment(String postId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(" Not Found"+ postId));

        User user = getCurrentUser();

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setUser(user);
        comment.setDepth(0);

        if (request.getParentCommentId() != null) {
            processParentComment(comment, request.getParentCommentId());
        }
        CommentResponse response = saveAndConvert(comment);

        notificationService.sendCommentNotification(postId, response);
        notificationService.sendGlobalNotification("Cha này mới comment: " + response.getAuthorUsername()  + "bài" + post.getTitle() );
        return response;
    }

    private void processParentComment(Comment comment, String parentId) {
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException(" Not Found"+  parentId));

        if (parent.getDepth() >= MAX_REPLY_DEPTH) {
            throw new CommentDepthException("Maximum reply depth of " + MAX_REPLY_DEPTH + " reached");
        }

        comment.setParentComment(parent);
        comment.setDepth(parent.getDepth() + 1);

        parent.addReply(comment);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#postId + '-' + #pageable.pageNumber")
    public Page<CommentResponse> getTopLevelComments(String postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(
                postId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by("createdAt").descending()
                )
        );
        return comments.map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "replies", key = "#commentId + '-' + #pageable.pageNumber")
    public Page<CommentResponse> getReplies(String commentId, Pageable pageable) {
        Page<Comment> replies = commentRepository.findByParentCommentId(
                commentId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by("createdAt").descending()
                )
        );
        return replies.map(this::convertToResponse);
    }

    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = modelMapper.map(comment, CommentResponse.class);
        response.setAuthorUsername(comment.getUser().getUsername());

        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }

        if (comment.getDepth() < MAX_REPLY_DEPTH && !comment.getReplies().isEmpty()) {
            List<CommentResponse> replyResponses = comment.getReplies().stream()
                    .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                    .limit(MAX_REPLIES_PER_LEVEL)
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            response.setReplies(replyResponses);
            response.setHasMoreReplies(comment.getReplyCount() > MAX_REPLIES_PER_LEVEL);
            response.setReplyCount(comment.getReplyCount());
        }

        return response;
    }

    private CommentResponse saveAndConvert(Comment comment) {
        Comment saved = commentRepository.save(comment);
        return convertToResponse(saved);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }
}