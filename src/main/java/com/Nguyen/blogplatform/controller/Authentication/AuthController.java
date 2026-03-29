package com.Nguyen.blogplatform.controller.Authentication;

import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.exception.TokenRefreshException;
import com.Nguyen.blogplatform.model.RefreshToken;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.LoginRequest;
import com.Nguyen.blogplatform.payload.request.SignupRequest;
import com.Nguyen.blogplatform.payload.response.JwtResponse;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.TokenRefreshResponse;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import com.Nguyen.blogplatform.service.auth.AuthService;
import com.Nguyen.blogplatform.service.auth.RefreshTokenService;
import com.Nguyen.blogplatform.service.auth.UserDetailsImpl;
import com.Nguyen.blogplatform.validation.annotation.WithRateLimitProtection;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for authentication endpoints.
 *
 * Responsibilities (controller layer only):
 *  - Route HTTP requests to the appropriate service method
 *  - Extract cookie / principal from the HTTP context
 *  - Build HTTP responses (headers, status codes)
 *
 * Business logic (validation, token creation, role resolution) lives in AuthService.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${blog.app.refreshTokenCookieName}")
    private String refreshTokenCookieName;

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    // -------------------------------------------------------------------------
    // POST /login
    // -------------------------------------------------------------------------

    @PostMapping("/login")
    @WithRateLimitProtection
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.authenticateUser(request);
    }

    // -------------------------------------------------------------------------
    // POST /register
    // -------------------------------------------------------------------------

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody SignupRequest request) {
        return authService.registerUser(request);
    }

    // -------------------------------------------------------------------------
    // POST /refresh-token
    // -------------------------------------------------------------------------

    /**
     * Issues a new access token + rotated refresh token from the refresh token cookie.
     * Cookie extraction stays here because it requires HttpServletRequest,
     * which is an HTTP-layer concern, not a business-logic concern.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(HttpServletRequest request) {
        String rawToken = extractRefreshTokenCookie(request);

        return refreshTokenService.findByToken(rawToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtUtils.generateTokenFromUserId(user.getId(), user.getEmail());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

                    ResponseCookie jwtCookie = ResponseCookie
                            .from(jwtUtils.getJwtCookie(), newAccessToken)
                            .path("/")
                            .maxAge(7 * 24 * 60 * 60)
                            .httpOnly(true)
                            .sameSite("Lax")
                            .build();

                    ResponseCookie refreshCookie =
                            refreshTokenService.generateRefreshTokenCookie(newRefreshToken.getToken());

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                            .body(new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new TokenRefreshException(rawToken, "Refresh token not found in database."));
    }

    // -------------------------------------------------------------------------
    // POST /logout
    // -------------------------------------------------------------------------

    /**
     * Deletes the refresh token from the DB and clears both auth cookies.
     * Requires an authenticated principal — secured via Spring Security config.
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        UserDetailsImpl userDetails = currentPrincipal();
        refreshTokenService.deleteByUserId(userDetails.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtUtils.getCleanJwtCookie().toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenService.getCleanRefreshTokenCookie().toString())
                .body(new MessageResponse("You've been signed out!"));
    }

    // -------------------------------------------------------------------------
    // GET /me
    // -------------------------------------------------------------------------

    /**
     * Returns the profile of the currently authenticated user.
     *
     * Authentication null / not-authenticated cases are handled by Spring Security
     * before reaching this method (401 is returned automatically). The manual
     * checks in the original code are therefore redundant and removed.
     *
     * A dedicated UserProfileResponse DTO is preferred over a raw Map — swap in
     * one when the response shape stabilises.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new NotFoundException("User not found: " + userDetails.getId()));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        Map<String, Object> body = Map.of(
                "id",       userDetails.getId(),
                "username", userDetails.getUsername(),
                "email",    userDetails.getEmail(),
                "avatar",   user.getAvatar() != null ? user.getAvatar() : "",
                "roles",    roles
        );

        return ResponseEntity.ok(body);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Reads the refresh token value from the incoming cookies.
     * Throws {@link TokenRefreshException} (→ 403) if the cookie is absent.
     */
    private String extractRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(c -> refreshTokenCookieName.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElseThrow(() -> new TokenRefreshException(null, "Refresh token cookie is missing."));
        }
        throw new TokenRefreshException(null, "Refresh token cookie is missing.");
    }

    /** Convenience cast — safe because this is only called from secured endpoints. */
    private UserDetailsImpl currentPrincipal() {
        return (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}