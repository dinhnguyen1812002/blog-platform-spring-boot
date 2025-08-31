package com.Nguyen.blogplatform.controller.Profile;

import com.Nguyen.blogplatform.payload.request.CustomProfileRequest;
import com.Nguyen.blogplatform.payload.response.UserProfileResponse;
import com.Nguyen.blogplatform.service.UserDetailsImpl;
import com.Nguyen.blogplatform.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile/custom")
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
}
