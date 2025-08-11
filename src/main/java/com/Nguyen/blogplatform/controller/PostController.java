package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.security.JwtUtils;
import com.Nguyen.blogplatform.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
public class PostController {

    @Autowired
    PostService postServices;

    @Autowired
    private JwtUtils jwtUtils;

    // @GetMapping("/{id}")
    // public ResponseEntity<PostResponse> getPostDetail(@PathVariable String id) {
    //     try {
    //         PostResponse postDetail = postServices.getPostById(id);
    //         return ResponseEntity.ok(postDetail);
    //     } catch (Exception e) {
    //         throw new NotFoundException("Post not found with id: " + id);
    //     }
    // }

    @PostMapping("/{id}/like")
    public ResponseEntity<Boolean> toggleLike(@PathVariable String id) {
        boolean isLiked = postServices.toggleLike(id);
        return ResponseEntity.ok(isLiked);
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Integer> ratePost(@PathVariable String id, @RequestParam Integer score) {
        Integer savedScore = postServices.ratePost(id, score);
        return ResponseEntity.ok(savedScore);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponse> posts = postServices.getListPost(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable(name = "slug") String slug) {
        try {
            PostResponse postDetail = postServices.getPostBySlug(slug);
            return ResponseEntity.ok(postDetail);
        } catch (NotFoundException e) {
            throw new NotFoundException("Post not found with slug: " + slug);
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<List<PostResponse>> getLatestPosts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        Pageable pageable = Pageable.ofSize(limit);
        List<PostResponse> posts = postServices.getLatestPosts(pageable);
        return ResponseEntity.ok(posts);
    }
//@GetMapping("/latest")
//    public ResponseEntity<List<Post>> getLatesPost(){
//
//        return ResponseEntity.ok(postServices.getAllPost());
//    }

    @GetMapping("/featured")
    public ResponseEntity<List<PostResponse>> getFeaturedPosts() {
        List<PostResponse> featuredPosts = postServices.getFeaturedPosts(Pageable.ofSize(5));
        return ResponseEntity.ok(featuredPosts);
    }

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