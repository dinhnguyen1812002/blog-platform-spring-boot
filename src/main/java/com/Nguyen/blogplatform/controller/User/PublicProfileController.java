package com.Nguyen.blogplatform.controller.User;

import com.Nguyen.blogplatform.payload.response.PublicProfileResponse;
import com.Nguyen.blogplatform.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class PublicProfileController {

    private static final Logger log = LoggerFactory.getLogger(PublicProfileController.class);
    private final UserProfileService userProfileService;

    @GetMapping("/profile/{slug}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(@PathVariable String slug) {
        log.debug("Fetching public profile for slug={}", slug);
        PublicProfileResponse response = userProfileService.getPublicProfileBySlug(slug);
        return ResponseEntity.ok(response);
    }
}

