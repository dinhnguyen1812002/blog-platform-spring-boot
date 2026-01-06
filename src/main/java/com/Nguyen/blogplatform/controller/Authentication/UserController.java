package com.Nguyen.blogplatform.controller.Authentication;

import com.Nguyen.blogplatform.Enum.ESocialMediaPlatform;
import com.Nguyen.blogplatform.exception.ConflictException;
import com.Nguyen.blogplatform.exception.TokenExpiredException;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.UserProfileUpdateRequest;
import com.Nguyen.blogplatform.payload.response.AvatarUploadResponse;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.UserProfileResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.service.UserDetailsImpl;
import com.Nguyen.blogplatform.service.UserProfileService;
import com.Nguyen.blogplatform.service.UserServices;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/v1/user")
@RestController
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserServices userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileService userProfileService;



    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserInfo(@PathVariable String id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }


        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),

                user.getEmail(),
                user.getAvatar(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList())
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request) throws ConflictException {
        UserProfileResponse response = userProfileService.updateUserProfile(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserProfileResponse> patchProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody UserProfileUpdateRequest request) throws ConflictException {
        UserProfileResponse response = userProfileService.updateUserProfile(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/avatar", consumes = {"multipart/form-data"})
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file
    ) {
        var resp = userProfileService.uploadAvatar(userDetails.getId(), file);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserProfileResponse response = userProfileService.getUserProfile(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileResponse> getProfileById(@PathVariable String userId) {
        UserProfileResponse response = userProfileService.getUserProfileById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/stats")
    public ResponseEntity<?> getStatus () {

        Map<String, Integer> status = new HashMap<>();
        status.put("totalPosts", 10);
        status.put("totalViews", 110);
        status.put("totalComments", 10);
        status.put("totalLikes", 10);
        status.put("averageRating", 10);
        System.out.println(status);
        return ResponseEntity.ok(status);
    }

}
