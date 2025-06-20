package com.Nguyen.blogplatform.controller;


import com.Nguyen.blogplatform.model.Post;

import com.Nguyen.blogplatform.payload.request.PostRequest;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.service.AuthorServices;
import com.Nguyen.blogplatform.service.PostService;
import com.Nguyen.blogplatform.service.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/author")
public class AuthorController {

    @Autowired
    AuthorServices authorServices;
    @Autowired
    PostService postServices;

    @GetMapping("/posts")
    public List<PostResponse> getMyPosts(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return authorServices.getPostsForCurrentUser(page, size);
    }
    @PostMapping("/write")
    public ResponseEntity<MessageResponse> createPost(@RequestBody PostRequest postDTO ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String authorId = userDetails.getId();

        try {
            Post createdPost = authorServices.newPost(postDTO, authorId);
            return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("Post created successfully"));
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console
            // Hoặc dùng logger nếu có: logger.error("Error creating post", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String id,
            @RequestBody PostRequest postRequest) {
        PostResponse postResponse = authorServices.updatePost(id, postRequest);
        return ResponseEntity.ok(postResponse);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String postId) {
        authorServices.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

}
