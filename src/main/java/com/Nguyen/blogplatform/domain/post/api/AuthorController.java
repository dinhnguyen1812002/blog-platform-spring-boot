package com.Nguyen.blogplatform.domain.post.api;



import com.Nguyen.blogplatform.domain.post.application.AuthorServices;
import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.dto.PostRequest;
import com.Nguyen.blogplatform.domain.post.dto.PostResponse;
import com.Nguyen.blogplatform.domain.post.dto.PostSummaryResponse;
import com.Nguyen.blogplatform.shared.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authors to manage their posts.
 */
@RestController
@RequestMapping("/api/v1/author")
@RequiredArgsConstructor
@Tag(name = "Author Post Management", description = "API for authors to manage their own posts")
public class AuthorController {

    private final AuthorServices authorServices;

    @Operation(summary = "Get posts of the current authenticated author")
    @GetMapping("/posts")
    public ResponseEntity<Page<PostSummaryResponse>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String tagName,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        return ResponseEntity.ok(authorServices.getPostsForCurrentUser(
                page, size, keyword, categoryName, tagName, sortDirection));
    }

    @Operation(summary = "Create a new post")
    @PostMapping("/write")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest postRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authorServices.createPost(postRequest));
    }

    @Operation(summary = "Update an existing post")
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String id,
            @Valid @RequestBody PostRequest postRequest) {
        return ResponseEntity.ok(authorServices.updatePost(id, postRequest));
    }

    @Operation(summary = "Delete a post")
    @DeleteMapping("/{postId}")
    public ResponseEntity<MessageResponse> deletePost(@PathVariable String postId) {
        authorServices.deletePost(postId);
        return ResponseEntity.ok(new MessageResponse("Post deleted successfully"));
    }

    @Operation(summary = "Get post detail for the author (including unpublished)")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostDetail(@PathVariable String postId) {
        // We reuse the update logic's detail retrieval if needed, 
        // or just have a specific method in AuthorServices.
        // For simplicity, let's assume AuthorServices has a getPostDetail method or just use the same logic.
        // Re-implementing a simple one here for now or adding to AuthorServices.
        return ResponseEntity.ok(authorServices.getPostDetail(postId));
    }
}
