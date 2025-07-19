package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Bookmark;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.SavePostRequest;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.SavedPostResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.repository.BookmarkRepository;
import com.Nguyen.blogplatform.repository.PostRepository;

import com.Nguyen.blogplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookmarkService {
    
    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostService postService;
    
    @Transactional
    public MessageResponse savePost(String postId, SavePostRequest request) {
        User currentUser = getCurrentUser();

        log.info("User {} is saving post {}", currentUser);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        
        // Check if post is already saved by this user
        Optional<Bookmark> existingSavedPost = bookmarkRepository.findByUserAndPost(currentUser, post);
        
        if (existingSavedPost.isPresent()) {
            // Update notes if provided
            if (request != null && request.getNotes() != null) {
                Bookmark savedPost = existingSavedPost.get();
                savedPost.setNotes(request.getNotes());
                bookmarkRepository.save(savedPost);
                return new MessageResponse("Post notes updated successfully!");
            }
            return new MessageResponse("Post is already saved!");
        }
        
        // Create new saved post
        Bookmark savedPost = Bookmark.builder()
                .user(currentUser)
                .post(post)
                .notes(request != null ? request.getNotes() : null)
                .build();
        
        bookmarkRepository.save(savedPost);
        log.info("User {} saved post {}", currentUser.getUsername(), post.getTitle());
        
        return new MessageResponse("Post saved successfully!");
    }
    
    @Transactional
    public MessageResponse unsavePost(String postId) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        
        Bookmark savedPost = bookmarkRepository.findByUserAndPost(currentUser, post)
                .orElseThrow(() -> new NotFoundException("Saved post not found"));
        
        bookmarkRepository.delete(savedPost);
        log.info("User {} unsaved post {}", currentUser.getUsername(), post.getTitle());
        
        return new MessageResponse("Post removed from saved list successfully!");
    }
    
    public Page<SavedPostResponse> getUserSavedPosts(Pageable pageable) {
        User currentUser = getCurrentUser();
        return bookmarkRepository.findByUserOrderBySavedAtDesc(currentUser, pageable)
                .map(this::toSavedPostResponse);
    }
    
    public List<SavedPostResponse> getUserSavedPostsList() {
        User currentUser = getCurrentUser();
        return bookmarkRepository.findByUserOrderBySavedAtDesc(currentUser)
                .stream()
                .map(this::toSavedPostResponse)
                .collect(Collectors.toList());
    }
    
    public boolean isPostSavedByCurrentUser(String postId) {
        try {
            User currentUser = getCurrentUser();
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
            
            return bookmarkRepository.existsByUserAndPost(currentUser, post);
        } catch (Exception e) {
            // If user is not authenticated or post not found, return false
            return false;
        }
    }
    
    public Long getUserSavedPostsCount() {
        User currentUser = getCurrentUser();
        return bookmarkRepository.countByUser(currentUser);
    }
    
    public Long getPostSavedCount(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        
        return bookmarkRepository.countByPost(post);
    }
    
    @Transactional
    public MessageResponse updateSavedPostNotes(String savedPostId, SavePostRequest request) {
        User currentUser = getCurrentUser();
        Bookmark savedPost = bookmarkRepository.findById(savedPostId)
                .orElseThrow(() -> new NotFoundException("Saved post not found with id: " + savedPostId));
        
        // Check if the saved post belongs to the current user
        if (!savedPost.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own saved posts");
        }
        
        savedPost.setNotes(request.getNotes());
        bookmarkRepository.save(savedPost);
        
        return new MessageResponse("Saved post notes updated successfully!");
    }
    
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }
    
    private SavedPostResponse toSavedPostResponse(Bookmark savedPost) {
        // Get the full post response using PostService
        PostResponse postResponse = postService.getPostResponseById(savedPost.getPost().getId());
        
        return SavedPostResponse.builder()
                .id(savedPost.getId())
                .post(postResponse)
                .user(new UserResponse(
                    savedPost.getUser().getId(),
                    savedPost.getUser().getUsername(),
                    savedPost.getUser().getEmail()
                ))
                .notes(savedPost.getNotes())
                .savedAt(savedPost.getSavedAt())
                .build();
    }
}
