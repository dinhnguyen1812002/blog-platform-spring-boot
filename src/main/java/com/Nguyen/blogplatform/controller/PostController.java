package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.payload.request.PostRequest;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.security.JwtUtils;
import com.Nguyen.blogplatform.service.PostServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/post")
public class PostController {

    @Autowired
    PostServices postServices;
    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/{id}")
    public PostResponse getPostDetail(@PathVariable String id) {
        try {
            PostResponse postDetail = postServices.getPostByIdWithComments(id);
            return ResponseEntity.ok(postDetail).getBody();
        }catch (Exception e){
            throw new NotFoundException("Not found");
        }
    }
    @GetMapping("/latest")
    public ResponseEntity<Page<PostResponse>> getLatestPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostResponse> latestPosts = postServices.getListPost(pageable);
        return ResponseEntity.ok(latestPosts);
    }
    @GetMapping("/featured")
    public ResponseEntity<List<PostResponse>> getFeaturedPosts() {
        List<PostResponse> featuredPosts = postServices.getFeaturedPosts(Pageable.ofSize(5));
        return ResponseEntity.ok(featuredPosts);
    }
//    @PutMapping("/{id}")
//    public ResponseEntity<PostResponse> updatePost(
//            @PathVariable String id,
//            @RequestBody PostRequest postRequest) {
//        PostResponse postResponse = postServices.updatePost(id, postRequest);
//        return ResponseEntity.ok(postResponse);
//    }
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<PostResponse>> getPostsByCategory(@PathVariable Long categoryId) {
        List<PostResponse> posts = postServices.getPostsByCategory(categoryId);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId) {
        List<PostResponse> posts = postServices.searchPosts(title, categoryId);
        return ResponseEntity.ok(posts);
    }
}
