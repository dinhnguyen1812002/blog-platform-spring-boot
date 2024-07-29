package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.InvalidCategoryException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.PostRequest;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.repository.CategoryRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.PostSpecification;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CategoryRepository categoryRepository;

    public Post createPost(PostRequest postRequest) {
        User author = getCurrentUser();
        Set<Category> categories = getCategoriesFromIds(postRequest.getCategories());

        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());
        post.setAuthor(author);
        post.setCreatedAt(new Date());
        post.setCategories(categories);

        return postRepository.save(post);
    }

    public Page<PostResponse> getListPost(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::convertToPostResponse);
    }

    public List<PostResponse> getFeaturedPosts(Pageable limit) {
        List<Post> featuredPosts = postRepository.findByFeaturedTrueOrderByCreatedAtDesc(limit);
        return featuredPosts.stream()
                .map(this::convertToPostResponse)
                .collect(Collectors.toList());
    }

    public PostResponse getPostByIdWithComments(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        return mapPostToResponse(post);
    }

    public List<PostResponse> getPostsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        List<Post> posts = postRepository.findByCategoriesContaining(category);
        return posts.stream()
                .map(this::convertToPostResponse)
                .collect(Collectors.toList());
    }

    public List<PostResponse> searchPosts(String title, Long categoryId) {
        Specification<Post> spec = Specification.where(PostSpecification.hasTitle(title))
                .and(PostSpecification.hasCategoryId(categoryId));
        List<Post> posts = postRepository.findAll(spec);
        return posts.stream()
                .map(this::convertToPostResponse)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String jwt = (String) authentication.getCredentials();
        String userId = jwtUtils.getUserIdFromJwtToken(jwt);
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
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

    private PostResponse convertToPostResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthor().getUsername(),
                post.getTitle(),
                post.getCreatedAt(),
                post.getContent(),
                post.getImageUrl(),
                post.getCategories().stream().map(Category::getCategory).collect(Collectors.toSet())
        );
    }

    private PostResponse mapPostToResponse(Post post) {
        List<CommentResponse> commentResponses = post.getComments().stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getComment(),
                        comment.getCreatedAt(),
                        comment.getAuthor().getUsername()))
                .collect(Collectors.toList());

        return new PostResponse(
                post.getId(),
                post.getAuthor().getUsername(),
                post.getTitle(),
                post.getCreatedAt(),
                post.getFeatured(),
                post.getContent(),
                post.getImageUrl(),
                post.getCategories().stream().map(Category::getCategory).collect(Collectors.toSet()),
                commentResponses
        );
    }
}
