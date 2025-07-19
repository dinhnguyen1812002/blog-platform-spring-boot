package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.NotFoundException;

import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.response.UserProfileResponse;
import com.Nguyen.blogplatform.repository.BookmarkRepository;
import com.Nguyen.blogplatform.repository.CommentRepository;
import com.Nguyen.blogplatform.repository.PostRepository;

import com.Nguyen.blogplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BookmarkRepository savedPostRepository;
    private final CommentRepository commentRepository;
    
    public UserProfileResponse getUserProfile(UserDetailsImpl userDetails) {
        // Get user from database to get additional info
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Extract roles from authorities
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        
        // Get user statistics
        Long postsCount = postRepository.countByUser(user);
        Long savedPostsCount = savedPostRepository.countByUser(user);
        Long commentsCount = commentRepository.countByUser(user);
        
        return UserProfileResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .avatar(user.getAvatar())
                .roles(roles)
                .postsCount(postsCount)
                .savedPostsCount(savedPostsCount)
                .commentsCount(commentsCount)
                .build();
    }
    
    public UserProfileResponse getUserProfileById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Extract roles from user entity
//        List<ERole> roles = user.getRoles().stream()
//                .map(role -> role.getName())
//                .collect(Collectors.toList());
        
        // Get user statistics
        Long postsCount = postRepository.countByUser(user);
        Long savedPostsCount = savedPostRepository.countByUser(user);

        Long commentsCount = commentRepository.countByUser(user);
        
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
//                .roles(roles)
                .postsCount(postsCount)
                .savedPostsCount(savedPostsCount)
                .commentsCount(commentsCount)
                .build();
    }
}
