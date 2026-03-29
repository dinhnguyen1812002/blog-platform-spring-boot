package com.Nguyen.blogplatform.controller.Comment;

import com.Nguyen.blogplatform.payload.request.CommentRequest;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.service.auth.UserDetailsImpl;
import com.Nguyen.blogplatform.service.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * URL design:
 *
 *  POST   /api/v1/posts/{postId}/comments              → create comment / reply
 *  GET    /api/v1/posts/{postId}/comments              → list top-level comments (public)
 *  GET    /api/v1/comments/{commentId}/replies         → list replies (public)
 *  PUT    /api/v1/comments/{commentId}                 → edit comment (owner only)
 *  DELETE /api/v1/comments/{commentId}                 → delete comment (owner or admin)
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // POST /api/v1/posts/{postId}/comments
    @PostMapping("/api/v1/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
            @PathVariable String postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return commentService.createComment(postId, request, currentUser.getId());
    }

    // GET /api/v1/posts/{postId}/comments  — public
    @GetMapping("/api/v1/posts/{postId}/comments")
    public Page<CommentResponse> getComments(
            @PathVariable String postId,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return commentService.getTopLevelComments(postId, pageable);
    }

    // GET /api/v1/comments/{commentId}/replies  — public
    @GetMapping("/api/v1/comments/{commentId}/replies")
    public Page<CommentResponse> getReplies(
            @PathVariable String commentId,
            @PageableDefault(size = 5, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return commentService.getReplies(commentId, pageable);
    }

    // PUT /api/v1/comments/{commentId}  — owner only
    @PutMapping("/api/v1/comments/{commentId}")
    public CommentResponse updateComment(
            @PathVariable String commentId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return commentService.updateComment(commentId, request, currentUser.getId());
    }

    // DELETE /api/v1/comments/{commentId}  — owner OR admin
    @DeleteMapping("/api/v1/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentService.deleteComment(commentId, currentUser.getId(), isAdmin);
    }
}