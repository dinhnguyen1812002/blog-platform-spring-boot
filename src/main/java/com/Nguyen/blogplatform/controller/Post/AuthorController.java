package com.Nguyen.blogplatform.controller.Post;


import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.payload.request.PostRequest;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.service.AuthorServices;
import com.Nguyen.blogplatform.service.PostService;
import com.Nguyen.blogplatform.service.UserDetailsImpl;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/author")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthorController {

    @Autowired
    AuthorServices authorServices;
    @Autowired
    PostService postServices;

    @GetMapping("/posts")
    public List<PostResponse> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String tagName,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return authorServices.getPostsForCurrentUser(page, size, keyword, categoryName, tagName, sortDirection);
    }


    /**
     * Tạo bài viết mới
     *
     * Workflow:
     * 1. Client upload thumbnail qua /api/v1/upload trước (nếu có)
     * 2. Client gọi API này với thumbnail URL đã được trả về từ bước 1
     *
     * @param postRequest Thông tin bài viết (bao gồm thumbnail URL từ uploads controller)
     * @return PostResponse với thông tin bài viết đã tạo
     */
    @PostMapping(value = "/write")
    public ResponseEntity<MessageResponse> createPost(@Valid @RequestBody PostRequest postRequest) {
        try {
            // Lấy thông tin user với helper method
            UserDetailsImpl userDetails = getCurrentUserDetails();
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("User not authenticated or invalid authentication type"));
            }

            // Validate input
            if (postRequest == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Post request cannot be null"));
            }

            if (postRequest.getTitle() == null || postRequest.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Post title is required"));
            }

            if (postRequest.getContent() == null || postRequest.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Post content is required"));
            }

            String authorId = userDetails.getId();

            // Tạo post mới
            Post createdPost = authorServices.newPost(postRequest, authorId);

            // Convert to response using AuthorServices method
            PostResponse postResponse = authorServices.toPostResponse(createdPost);

            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Post created successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            // Log the full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error creating post: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật bài viết
     * @param id ID của bài viết
     * @param postRequest Thông tin cập nhật (thumbnail URL đã được xử lý qua uploads controller)
     * @return PostResponse đã cập nhật
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable String id,
            @Valid @RequestBody PostRequest postRequest) {
        try {
            PostResponse postResponse = authorServices.updatePost(id, postRequest);
            return ResponseEntity.ok(postResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating post: " + e.getMessage()));
        }
    }

    /**
     * Xóa bài viết
     * @param postId ID của bài viết cần xóa
     * @return Void response
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable String postId) {
        try {

            authorServices.deletePost(postId);
            return ResponseEntity.ok(new MessageResponse("Post deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error deleting post: " + e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết bài viết của tác giả
     * @param postId ID của bài viết
     * @return PostResponse
     */
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable String postId) {
        try {
            PostResponse postResponse = authorServices.getPostDetail(postId);
            return ResponseEntity.ok(postResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Post not found: " + e.getMessage()));
        }
    }

    /**
     * Helper method để lấy UserDetailsImpl một cách an toàn
     */
    private UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            return null;
        }

        return (UserDetailsImpl) principal;
    }


    /**
     * Helper method để convert Post thành PostResponse
     */
    private PostResponse convertToPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .thumbnail(post.getThumbnail())
                .createdAt(post.getCreatedAt())
                .featured(post.getFeatured())
                .viewCount(post.getView())
                .build();
    }

}
