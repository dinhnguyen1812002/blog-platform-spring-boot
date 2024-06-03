package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.model.Comment;
import com.Nguyen.blogplatform.payload.request.CommentRequest;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.service.CommentServices;
import com.Nguyen.blogplatform.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/comments")
@RestController
public class CommentController {
    @Autowired
    private CommentServices commentService;

    @PostMapping("/posts/{postId}")
    public ResponseEntity<CommentResponse> createComment(@PathVariable String postId, @RequestBody CommentRequest commentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        CommentResponse commentResponse = commentService.createComment(commentRequest, postId, userId);
        return ResponseEntity.ok(commentResponse);
    }
}
