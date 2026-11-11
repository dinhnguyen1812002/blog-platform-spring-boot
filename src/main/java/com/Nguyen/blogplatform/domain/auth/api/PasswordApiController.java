package com.Nguyen.blogplatform.domain.auth.api;

import com.Nguyen.blogplatform.domain.auth.application.RefreshTokenService;
import com.Nguyen.blogplatform.domain.auth.application.UserDetailsImpl;
import com.Nguyen.blogplatform.domain.user.application.UserServices;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.shared.dto.MessageResponse;
import com.Nguyen.blogplatform.shared.exception.TokenExpiredException;
import com.Nguyen.blogplatform.shared.validation.annotation.WithRateLimitProtection;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

/**
 * JSON password endpoints used by the SPA (frontend calls them under /api/v1).
 * The Thymeleaf form pages remain in {@link PasswordController} under /auth.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordApiController {

    private final UserServices userService;
    private final RefreshTokenService refreshTokenService;

    public record ForgotPasswordRequest(String email) {}

    public record ResetPasswordRequest(String token, String newPassword) {}

    public record UpdatePasswordRequest(String oldPassword, String newPassword) {}

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Always answers with the same message so the endpoint can't be used to
     * discover which emails are registered.
     */
    @PostMapping("/forgot-password")
    @WithRateLimitProtection
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request == null || isBlank(request.email())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email is required"));
        }
        try {
            userService.sendResetEmail(request.email().trim());
        } catch (UsernameNotFoundException e) {
            // Intentionally not revealed to the client
        } catch (MessagingException e) {
            log.error("Failed to send reset password email", e);
        }
        return ResponseEntity.ok(new MessageResponse(
                "If an account exists for this email, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    @WithRateLimitProtection
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request == null || isBlank(request.token()) || isBlank(request.newPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Token and newPassword are required"));
        }
        try {
            userService.resetPassword(request.token(), request.newPassword());
            return ResponseEntity.ok(new MessageResponse("Password has been reset"));
        } catch (TokenExpiredException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Token is invalid or expired"));
        }
    }

    /** Requires authentication (see SecurityConfig AUTHENTICATED_USER). */
    @PatchMapping("/update-password")
    @WithRateLimitProtection
    public ResponseEntity<MessageResponse> updatePassword(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestBody UpdatePasswordRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Unauthorized"));
        }
        if (request == null || isBlank(request.oldPassword()) || isBlank(request.newPassword())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Both oldPassword and newPassword must be provided"));
        }

        User user = userService.findById(principal.getId());
        try {
            userService.updatePassword(user, request.oldPassword(), request.newPassword());
            // One refresh token per user: issuing a new one signs out every other
            // device while keeping the current session alive.
            var refreshToken = refreshTokenService.createRefreshToken(user.getId());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE,
                            refreshTokenService.generateRefreshTokenCookie(refreshToken.getToken()).toString())
                    .body(new MessageResponse("Password updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
