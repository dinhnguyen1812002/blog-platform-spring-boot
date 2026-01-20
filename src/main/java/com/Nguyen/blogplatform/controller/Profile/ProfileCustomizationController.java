package com.Nguyen.blogplatform.controller.Profile;

import com.Nguyen.blogplatform.exception.ConflictException;
import com.Nguyen.blogplatform.payload.request.CustomProfileRequest;
import com.Nguyen.blogplatform.payload.request.UserProfileUpdateRequest;
import com.Nguyen.blogplatform.payload.response.AvatarUploadResponse;
import com.Nguyen.blogplatform.payload.response.UserProfileResponse;
import com.Nguyen.blogplatform.service.UserDetailsImpl;
import com.Nguyen.blogplatform.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileCustomizationController {

    private final UserProfileService userProfileService;

    /**
     * Update the custom profile markdown content for the authenticated user
     * 
     * @param userDetails The authenticated user details
     * @param request The request containing the markdown content
     * @return The updated user profile
     */
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateCustomProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CustomProfileRequest request) {

        UserProfileResponse response = userProfileService.updateUserProfileMarkdown(
                userDetails.getId(), 
                request.getMarkdownContent()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<UserProfileResponse> patchProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody UserProfileUpdateRequest request) throws ConflictException {
        UserProfileResponse response = userProfileService.updateUserProfile(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get the custom profile markdown content for a specific user
     * 
     * @param username The username of the user
     * @return The user profile with processed markdown content
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getCustomProfile(@PathVariable String username) {
        UserProfileResponse response = userProfileService.getUserProfileByUsername(username);
        return ResponseEntity.ok(response);
    }

//    @PutMapping("/profile")
//    public ResponseEntity<UserProfileResponse> updateProfile(
//            @AuthenticationPrincipal UserDetailsImpl userDetails,
//            @Valid @RequestBody UserProfileUpdateRequest request) throws ConflictException {
//        UserProfileResponse response = userProfileService.updateUserProfile(userDetails.getId(), request);
//        return ResponseEntity.ok(response);
//    }


    @PostMapping(value = "/avatar", consumes = {"multipart/form-data"})
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file
    ) {
        var resp = userProfileService.uploadAvatar(userDetails.getId(), file);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserProfileResponse response = userProfileService.getUserProfile(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfileById(@PathVariable String userId) {
        UserProfileResponse response = userProfileService.getUserProfileById(userId);
        return ResponseEntity.ok(response);
    }
}
