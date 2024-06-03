package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.InvalidCategoryException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.exception.UnauthorizedException;
import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.PostRequest;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.repository.CategoryRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import jakarta.transaction.Transactional;
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
    public List<PostResponse> getPostsForCurrentUser(int page, int size) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            String username = userDetails.getUsername();
            Pageable pageable = PageRequest.of(page, size);
            Page<Post> postPage = postRepository.findByAuthor(username, pageable);
            return postPage.stream()
                    .map(this::convertToPostDetailDTO)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalStateException("Unexpected principal type");
        }
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

    public Post newPost(PostRequest postRequest, String authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + authorId));
        Set<Category> categories = new HashSet<>();
        if (postRequest.getCategories() == null || postRequest.getCategories().isEmpty()) {
            throw new InvalidCategoryException("A post must have at least one category");
        }
        for (Long categoryId : postRequest.getCategories()) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
            categories.add(category);
        }
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setAuthor(author);
        post.setFeatured(postRequest.getFeatured());
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());
        post.setCreatedAt(postRequest.getCreatedAt() != null ? postRequest.getCreatedAt() : new Date());
        post.setCategories(categories);
        return postRepository.save(post);
    }

    public PostResponse updatePost(String postId, PostRequest postRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        if (!post.getAuthor().getUsername().equals(currentUsername)) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }

        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());

        Set<Category> categories = postRequest.getCategories().stream()
                .map(categoryId -> categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId)))
                .collect(Collectors.toSet());

        post.setCategories(categories);

        Post updatedPost = postRepository.save(post);
        return convertToPostResponse(updatedPost);
    }
    @Transactional
    public void deletePost(String postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        if (!post.getAuthor().getUsername().equals(currentUsername)) {
            throw new UnauthorizedException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    private PostResponse convertToPostResponse(Post post) {
        List<CommentResponse> commentResponses = post.getComments().stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getComment(),
                        comment.getCreatedAt(),
                        comment.getAuthor().getUsername()
                ))
                .collect(Collectors.toList());

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getImageUrl(),
                post.getAuthor().getUsername(),
                commentResponses
        );
    }
}
