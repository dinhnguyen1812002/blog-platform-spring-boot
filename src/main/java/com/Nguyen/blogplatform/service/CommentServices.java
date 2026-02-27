package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.CommentDepthException;
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
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
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
    private Logger log = Logger.getLogger(CommentServices.class.getName());

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "comments", key = "#postId"),
            @CacheEvict(value = "comments", key = "#postId + '-*'", allEntries = true),
            @CacheEvict(value = "replies", key = "#request.parentCommentId", condition = "#request.parentCommentId != null")
    })
    public CommentResponse createComment(String postId, CommentRequest request) {

        // Lưu lại SecurityContext hiện tại
        SecurityContext context = SecurityContextHolder.getContext();

        // Bắt đầu xử lý bất đồng bộ với context được truyền đúng
        CompletableFuture<Post> postFuture = CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(context);
            return postRepository.findById(postId)
                    .orElseThrow(() -> new NotFoundException("Post Not Found: " + postId));
        });

        CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(context);
            return getCurrentUser();
        });

        // Chờ cả hai hoàn tất
        CompletableFuture.allOf(postFuture, userFuture).join();

        // Lấy kết quả
        Post post = postFuture.join();
        User user = userFuture.join();

        // Tạo comment
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setUser(user);
        comment.setDepth(0);

        // Nếu là reply
        if (request.getParentCommentId() != null) {
            processParentComment(comment, request.getParentCommentId());
        }

        // Lưu và trả kết quả
        CommentResponse response = saveAndConvert(comment);

        // Gửi notification bất đồng bộ (không cần context nữa)
        // Notify the post author about the new comment
        if (!post.getUser().getId().equals(user.getId())) {  // Don't notify if the author is commenting on their own post
            CompletableFuture.runAsync(() -> {
                try {
                    notificationService.notifyPostAuthorAboutComment(
                        post.getUser().getId(),
                        user.getUsername(),
                        post.getTitle(),
                        postId
                    );
                } catch (Exception e) {
                    log.warning("Failed to send notification to post author: " + e.getMessage());
                }
            });
        }

        // Send real-time notification about the new comment
        CompletableFuture.runAsync(() -> {
            notificationService.sendCommentNotification(postId, response);
            notificationService.sendGlobalNotification(
                    "New comment from: " + response.getUser().getUsername() + " on post: " + post.getTitle());
        });

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
    @Cacheable(value = "comments", key = "#postId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<CommentResponse> getTopLevelComments(String postId, Pageable pageable) {
        // Create a standardized page request with consistent sorting
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("createdAt").descending()
        );

        // Use the optimized repository method
        Page<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(
                postId,
                pageRequest
        );

        // Process the comments in parallel for better performance with large result sets
        return comments.map(comment -> {
            try {
                // For very large result sets, this parallel processing can significantly improve performance
                if (comments.getTotalElements() > 20) {
                    return CompletableFuture.supplyAsync(() -> convertToResponse(comment)).join();
                } else {
                    // For smaller result sets, avoid the overhead of creating threads
                    return convertToResponse(comment);
                }
            } catch (Exception e) {
                // Log the error and return a simplified response in case of conversion errors
                // This prevents one bad comment from breaking the entire page
                CommentResponse fallback = new CommentResponse();
                fallback.setId(comment.getId());
                fallback.setContent("Error loading comment: " + e.getMessage());
                return fallback;
            }
        });
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "replies", key = "#commentId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<CommentResponse> getReplies(String commentId, Pageable pageable) {
        // Validate the comment exists before fetching replies
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        // Create a standardized page request with consistent sorting
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("createdAt").descending()
        );

        // Use the optimized repository method
        Page<Comment> replies = commentRepository.findByParentCommentId(
                commentId,
                pageRequest
        );

        // Process the replies in parallel for better performance with large result sets
        return replies.map(reply -> {
            try {
                // For very large result sets, this parallel processing can significantly improve performance
                if (replies.getTotalElements() > 10) {
                    return CompletableFuture.supplyAsync(() -> convertToResponse(reply)).join();
                } else {
                    // For smaller result sets, avoid the overhead of creating threads
                    return convertToResponse(reply);
                }
            } catch (Exception e) {
                // Log the error and return a simplified response in case of conversion errors
                CommentResponse fallback = new CommentResponse();
                fallback.setId(reply.getId());
                fallback.setContent("Error loading reply: " + e.getMessage());
                return fallback;
            }
        });
    }

    private CommentResponse convertToResponse(Comment comment) {
        // Use direct field mapping instead of ModelMapper for better performance
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setDepth(comment.getDepth());
        response.setReplyCount(comment.getReplyCount());

        // Safely get username to avoid NPE
      if(comment.getUser() != null) {
          UserResponse userResponse = new UserResponse();
          userResponse.setId(comment.getUser().getId());
          userResponse.setUsername(comment.getUser().getUsername());
          userResponse.setEmail(comment.getUser().getEmail());
          userResponse.setAvatar(comment.getUser().getAvatar());
          response.setUser(userResponse);

        }

        // Set parent comment ID if exists
        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }

        // Process replies only if needed and within depth limit
        if (comment.getDepth() < MAX_REPLY_DEPTH && comment.getReplyCount() > 0) {
            // Only load replies if they exist and we're within depth limit
            if (!comment.getReplies().isEmpty()) {
                // Use parallel stream for better performance with larger reply sets
                List<CommentResponse> replyResponses = comment.getReplies().stream()
                        .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                        .limit(MAX_REPLIES_PER_LEVEL)
                        .parallel()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList());

                response.setReplies(replyResponses);
            }

            // Set flag for pagination UI
            response.setHasMoreReplies(comment.getReplyCount() > MAX_REPLIES_PER_LEVEL);
        }

        return response;
    }

    private CommentResponse saveAndConvert(Comment comment) {
        try {
            // Ensure the comment has timestamps set
            if (comment.getCreatedAt() == null) {
                comment.prePersist();
            }

            // Save the comment to the database
            Comment saved = commentRepository.save(comment);

            // Convert to response object
            return convertToResponse(saved);
        } catch (Exception e) {
            // Log the error and rethrow to ensure transaction rollback
            throw new RuntimeException("Error saving comment: " + e.getMessage(), e);
        }
    }



    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId(); // Lấy id từ UserDetailsImpl
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
}
