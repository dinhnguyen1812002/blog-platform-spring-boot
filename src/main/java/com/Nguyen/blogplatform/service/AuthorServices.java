package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Utils.SlugUtil;
import com.Nguyen.blogplatform.exception.InvalidCategoryException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.exception.UnauthorizedException;
import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.PostRequest;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.repository.CategoryRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthorServices {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NotificationService notificationService;


    public static final Boolean FEATURED = false;
    public List<PostResponse> getPostsForCurrentUser(int page, int size) {
        String username = getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByUser(username, pageable).stream()
                .map(this::convertToPostDetailDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Post newPost(PostRequest postRequest, String authorId) throws BadRequestException {
        if (postRequest == null || postRequest.getTitle() == null || postRequest.getTitle().isBlank()) {
            throw new BadRequestException("Dữ liệu đầu vào không hợp lệ");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + authorId));

        Set<Category> categories = getCategoriesFromIds(postRequest.getCategories());
        String slug = SlugUtil.createSlug(postRequest.getTitle());
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setSlug(slug);
        post.setUser(author);
        post.setFeatured(FEATURED);
        post.setContent(postRequest.getContent() != null ? postRequest.getContent() : "");
        post.setImageUrl(postRequest.getImageUrl() != null ? postRequest.getImageUrl() : "");
        post.setCreatedAt(postRequest.getCreatedAt() != null ? postRequest.getCreatedAt() : new Date());
        post.setCategories(categories);

        try {
            Post savedPost = postRepository.save(post);
            notificationService.sendPostNotification(savedPost.getId(), "New post created: " + postRequest.getTitle());
            notificationService.sendGlobalNotification("New post available: " + savedPost.getTitle());
            return savedPost;
        } catch (Exception e) {
            throw new RuntimeException("Error saving post", e); // Ném lại để log chi tiết
        }
    }

    public PostResponse updatePost(String postId, PostRequest postRequest) {
        String currentUsername = getCurrentUsername();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        if (!post.getUser().getUsername().equals(currentUsername)) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }

        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());
        post.setCategories(getCategoriesFromIds(postRequest.getCategories()));

        return convertToPostResponse(postRepository.save(post));
    }

    @Transactional
    public void deletePost(String postId) {
        String currentUsername = getCurrentUsername();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        if (!post.getUser().getUsername().equals(currentUsername)) {
            throw new UnauthorizedException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private Set<Category> getCategoriesFromIds(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new InvalidCategoryException("A post must have at least one category");
        }
        return categoryIds.stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Category not found with id: " + id)))
                .collect(Collectors.toSet());
    }

    private PostResponse convertToPostDetailDTO(Post post) {
        return getPostResponse(post);
    }

    private PostResponse convertToPostResponse(Post post) {
        return getPostResponse(post);
    }

    static PostResponse getPostResponse(Post post) {
//        List<CommentResponse> commentResponses = post.getContent().stream()
//                .map(comment -> new CommentResponse(
//                        comment.getId(),
//                        comment.getComment(),
//                        comment.getCreatedAt(),
//                        comment.getAuthor().getUsername()
//                ))
//                .collect(Collectors.toList());
        User user  = post.getUser();

        UserResponse userResponse =  new UserResponse(user.getId(), user.getUsername(), user.getEmail());
        return new PostResponse(
                post.getId(),
                userResponse,
                post.getTitle(),
                post.getSlug(),
                post.getCreatedAt(),
                post.getFeatured(),
                post.getContent(),
                post.getImageUrl(),
                post.getCategories().stream().map(Category::getCategory).collect(Collectors.toSet())
//                commentResponses
        );
    }
}
