package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.InvalidCategoryException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.exception.UnauthorizedException;
import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.model.Comment;
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
public class PostServices {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private CategoryRepository categoryRepository;
//    public Post createPost(PostRequest postRequest) {
//        Set<Category> categories = new HashSet<>();
//        for (Long categoryId : postRequest.getCategoryId()) {
//            Category category = categoryRepository.findById(categoryId)
//                    .orElseThrow(() -> new RuntimeException("Error: Category not found."));
//            categories.add(category);
//        }
//
//        Post post = new Post(postRequest.getTitle(), postRequest.getContent(), postRequest.getImageUrl(), user);
//        post.setCategories(categories);
//        return postRepository.save(post);
//    }
    public Post createPost(PostRequest postDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String jwt = (String) authentication.getCredentials();
        String userId = jwtUtils.getUserIdFromJwtToken(jwt);

        User author = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Set<Category> categories = new HashSet<>();
        if (postDTO.getCategories() == null || postDTO.getCategories().isEmpty()) {
            throw new InvalidCategoryException("A post must have at least one category");
        }
        for (Long categoryId : postDTO.getCategories()) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
            categories.add(category);
        }

        Post post = new Post();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setImageUrl(postDTO.getImageUrl());
        post.setAuthor(author);
        post.setCreatedAt(new Date());
        post.setCategories(categories);

        return postRepository.save(post);
    }
    public Page<PostResponse> getListPost(Pageable pageable) {
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return posts.map(this::convertToPostDetailDTO);
    }
    private PostResponse convertToPostDetailDTO(Post post) {
        PostResponse postDetailDTO = new PostResponse();
        postDetailDTO.setId(post.getId());
        postDetailDTO.setAuthorName(post.getAuthor().getUsername());
        postDetailDTO.setTitle(post.getTitle());
        postDetailDTO.setCreatedAt(post.getCreatedAt());
        postDetailDTO.setFeatured(post.getFeatured());
        postDetailDTO.setImageUrl(post.getImageUrl());
        postDetailDTO.setCategories(post.getCategories().stream().map(Category::getCategory).collect(Collectors.toSet()));
        return postDetailDTO;
    }
    public List<PostResponse> getFeaturedPosts(Pageable limit) {
        List<Post> featuredPosts = postRepository.findByFeaturedTrueOrderByCreatedAtDesc(limit);
        return convertPostsToResponse(featuredPosts);
    }

    private List<PostResponse> convertPostsToResponse(List<Post> featuredPosts) {
        List<PostResponse> responses = new ArrayList<>();
        for (Post post : featuredPosts) {
            PostResponse response = new PostResponse(
                    post.getId(),
                    post.getAuthor().getUsername(),
                    post.getTitle(),
                    post.getCreatedAt(),
                    post.getContent(),
                    post.getImageUrl(),
                    post.getCategories().stream().map(Category::getCategory).collect(Collectors.toSet())
            );
            responses.add(response);
        }
        return responses;
    }
    public PostResponse getPostByIdWithComments(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        return mapPostToResponse(post);
    }

    private PostResponse mapPostToResponse(Post post) {
        List<CommentResponse> commentResponses = new ArrayList<>();
        for (Comment comment : post.getComments()) {
            CommentResponse commentResponse = new CommentResponse(
                    comment.getId(),
                    comment.getComment(),
                    comment.getCreatedAt(),
                    comment.getAuthor().getUsername()
            );
            commentResponses.add(commentResponse);
        }

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
    public List<PostResponse> getPostsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        List<Post> posts = postRepository.findByCategoriesContaining(category);
        return posts.stream()
                .map(this::convertToPostDetailDTO)
                .collect(Collectors.toList());
    }

    public List<PostResponse> searchPosts(String title, Long categoryId) {
        Specification<Post> spec = Specification.where(PostSpecification.hasTitle(title))
                .and(PostSpecification.hasCategoryId(categoryId));
        List<Post> posts = postRepository.findAll(spec);
        return posts.stream()
                .map(this::convertToPostDetailDTO)
                .collect(Collectors.toList());
    }
}
