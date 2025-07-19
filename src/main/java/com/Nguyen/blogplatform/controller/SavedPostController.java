package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.payload.request.SavePostRequest;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.SavedPostResponse;
import com.Nguyen.blogplatform.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/saved-posts")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('USER')")
@Tag(name = "Saved Posts", description = "User saved posts management")
public class SavedPostController {
    
    private final BookmarkService savedPostService;
    
    @PostMapping("/{postId}")
    @Operation(summary = "Save a post", description = "Save a post to user's saved list with optional notes")
    public ResponseEntity<MessageResponse> savePost(
            @Parameter(description = "Post ID to save") @PathVariable String postId,
            @Valid @RequestBody(required = false) SavePostRequest request) {
        MessageResponse response = savedPostService.savePost(postId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{postId}")
    @Operation(summary = "Unsave a post", description = "Remove a post from user's saved list")
    public ResponseEntity<MessageResponse> unsavePost(
            @Parameter(description = "Post ID to unsave") @PathVariable String postId) {
        MessageResponse response = savedPostService.unsavePost(postId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get saved posts", description = "Get paginated list of user's saved posts")
    public ResponseEntity<Page<SavedPostResponse>> getSavedPosts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SavedPostResponse> savedPosts = savedPostService.getUserSavedPosts(pageable);
        return ResponseEntity.ok(savedPosts);
    }
    
    @GetMapping("/list")
    @Operation(summary = "Get all saved posts", description = "Get complete list of user's saved posts without pagination")
    public ResponseEntity<List<SavedPostResponse>> getSavedPostsList() {
        List<SavedPostResponse> savedPosts = savedPostService.getUserSavedPostsList();
        return ResponseEntity.ok(savedPosts);
    }
    
    @GetMapping("/check/{postId}")
    @Operation(summary = "Check if post is saved", description = "Check if a specific post is saved by current user")
    public ResponseEntity<Boolean> isPostSaved(
            @Parameter(description = "Post ID to check") @PathVariable String postId) {
        boolean isSaved = savedPostService.isPostSavedByCurrentUser(postId);
        return ResponseEntity.ok(isSaved);
    }
    
    @GetMapping("/count")
    @Operation(summary = "Get saved posts count", description = "Get total count of user's saved posts")
    public ResponseEntity<Long> getSavedPostsCount() {
        Long count = savedPostService.getUserSavedPostsCount();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/post/{postId}/count")
    @Operation(summary = "Get post saved count", description = "Get total count of how many users saved a specific post")
    public ResponseEntity<Long> getPostSavedCount(
            @Parameter(description = "Post ID") @PathVariable String postId) {
        Long count = savedPostService.getPostSavedCount(postId);
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/notes/{savedPostId}")
    @Operation(summary = "Update saved post notes", description = "Update notes for a saved post")
    public ResponseEntity<MessageResponse> updateSavedPostNotes(
            @Parameter(description = "Saved post ID") @PathVariable String savedPostId,
            @Valid @RequestBody SavePostRequest request) {
        MessageResponse response = savedPostService.updateSavedPostNotes(savedPostId, request);
        return ResponseEntity.ok(response);
    }
}
