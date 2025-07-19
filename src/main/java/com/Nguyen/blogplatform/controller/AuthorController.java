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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/v1/author")
@CrossOrigin(origins = "*", allowedHeaders = "*")
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

    //use with upload controller
    @PostMapping(value = "/write")
    public ResponseEntity<MessageResponse> createPost(
            @RequestBody PostRequest postRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not authenticated"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String authorId = userDetails.getId();

        try {
            Post createdPost = authorServices.newPost(postRequest, authorId);
            return ResponseEntity.ok(new MessageResponse("Post created successfully"));
        } catch (Exception e) {
            // Log the full stack trace
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error creating post: " + e.getMessage()));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String id,
            @RequestBody PostRequest postRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        PostResponse postResponse = authorServices.updatePost(id, postRequest, imageFile);
        return ResponseEntity.ok(postResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String postId) {
        authorServices.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

}
