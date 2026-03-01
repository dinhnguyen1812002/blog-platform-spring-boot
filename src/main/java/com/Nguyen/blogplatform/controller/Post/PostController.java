package com.Nguyen.blogplatform.controller.Post;

import com.Nguyen.blogplatform.exception.NotFoundException;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
public class PostController {

    @Autowired
    PostService postServices;

    @Autowired
    private JwtUtils jwtUtils;

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

    // @GetMapping
    // public ResponseEntity<Page<PostResponse>> getPosts(
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "5") int size
    // ) {
    // Pageable pageable = PageRequest.of(page, size,
    // Sort.by("createdAt").descending());
    // Page<PostResponse> posts = postServices.getListPost(pageable);
    // return ResponseEntity.ok(posts);
    // }

    @Operation(summary = "Lấy chi tiết bài viết theo slug (Công khai)", description = "Lấy bài viết công khai cho tất cả người đọc. Các quy tắc áp dụng: <br> - Bài viết **PRIVATE**: Trả về 403 Forbidden nếu người xem không phải tác giả.<br> - Bài viết **SCHEDULED**: Trả về 404 Not Found nếu ngày xuất bản (scheduledPublishAt) chưa đến.<br> - Bài viết **DRAFT**: Trả về 404 Not Found.<br> - Bài viết **PUBLISHED**: Luôn hiển thị.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trả về chi tiết bài viết", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class)) }),
            @ApiResponse(responseCode = "403", description = "Bài viết là PRIVATE và người gọi không phải tác giả", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết, bài viết là DRAFT, hoặc bài viết là SCHEDULED chưa tới thời gian xuất bản", content = @Content)
    })
    @GetMapping("/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable(name = "slug") String slug) {
        try {
            PostResponse postDetail = postServices.getPostBySlug(slug);
            return ResponseEntity.ok(postDetail);
        } catch (NotFoundException e) {
            throw new NotFoundException("Post not found with slug: " + slug);
        }
    }

    /**
     * Enhanced endpoint to get latest/filtered posts with pagination, sorting
     * (newest or views),
     * and filters by category slug and tag slug.
     *
     * @param page         Page number (default: 0).
     * @param size         Page size (default: 10).
     * @param sortBy       Sorting criteria: "newest" (default) or "views".
     * @param categorySlug Optional category slug filter.
     * @param tagSlug      Optional tag slug filter.
     * @return Page of PostResponse objects.
     */
    @GetMapping()
    public ResponseEntity<Page<PostResponse>> getLatestPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String tagSlug) {
        Sort sort;

        if ("views".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("view").descending();
        } else {
            sort = Sort.by("createdAt").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PostResponse> posts = postServices.getFilteredPosts(categorySlug, tagSlug, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<PostResponse>> getFeaturedPosts() {
        List<PostResponse> featuredPosts = postServices.getFeaturedPosts(Pageable.ofSize(4));
        return ResponseEntity.ok(featuredPosts);
    }

    @GetMapping("/category/{slug}")
    public ResponseEntity<List<PostResponse>> getPostsByCategory(@PathVariable String slug) {
        List<PostResponse> posts = postServices.getPostsByCategory(slug);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId) {
        List<PostResponse> posts = postServices.searchPosts(title, categoryId);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/{id}/featured")
    public ResponseEntity<PostResponse> toggleFeatured(@PathVariable String id) {
        PostResponse postResponse = postServices.toggleFeatured(id);
        return ResponseEntity.ok(postResponse);
    }
}