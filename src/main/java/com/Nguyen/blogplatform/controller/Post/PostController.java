package com.Nguyen.blogplatform.controller.Post;

import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.PostSummaryResponse;
import com.Nguyen.blogplatform.service.post.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public REST Controller for Post management.
 */
@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
@Tag(name = "Post", description = "Public Post Management API")
public class PostController {

    private final PostService postService;

    @Operation(summary = "Get latest/filtered posts with pagination")
    @GetMapping
    public ResponseEntity<Page<PostSummaryResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String tagSlug) {
        
        Sort sort = "views".equalsIgnoreCase(sortBy) ? Sort.by("viewCount").descending() : Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PostSummaryResponse> posts = postService.getFilteredPosts(categorySlug, tagSlug, pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Get post details by slug")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post found", content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found or not published"),
            @ApiResponse(responseCode = "403", description = "Post is private")
    })
    @GetMapping("/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostBySlug(slug));
    }

    @Operation(summary = "Get featured posts")
    @GetMapping("/featured")
    public ResponseEntity<List<PostSummaryResponse>> getFeaturedPosts() {
        return ResponseEntity.ok(postService.getFeaturedPosts(PageRequest.of(0, 4)));
    }

    @Operation(summary = "Toggle like on a post")
    @PostMapping("/{id}/like")
    public ResponseEntity<Boolean> toggleLike(@PathVariable String id) {
        return ResponseEntity.ok(postService.toggleLike(id));
    }

    @Operation(summary = "Rate a post")
    @PostMapping("/{id}/rate")
    public ResponseEntity<Integer> ratePost(@PathVariable String id, @RequestParam Integer score) {
        return ResponseEntity.ok(postService.ratePost(id, score));
    }

    @Operation(summary = "Toggle featured status (Admin only)")
    @PostMapping("/{id}/featured")
    public ResponseEntity<PostResponse> toggleFeatured(@PathVariable String id) {
        return ResponseEntity.ok(postService.toggleFeatured(id));
    }
}
