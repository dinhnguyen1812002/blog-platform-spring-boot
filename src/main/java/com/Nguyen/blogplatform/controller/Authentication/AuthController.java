package com.Nguyen.blogplatform.controller.Authentication;

import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.exception.TokenRefreshException;
import com.Nguyen.blogplatform.model.RefreshToken;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.LoginRequest;
import com.Nguyen.blogplatform.payload.request.SignupRequest;
import com.Nguyen.blogplatform.payload.request.TokenRefreshRequest;
import com.Nguyen.blogplatform.payload.response.JwtResponse;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.TokenRefreshResponse;
import com.Nguyen.blogplatform.repository.RoleRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import com.Nguyen.blogplatform.service.AuthService;
import com.Nguyen.blogplatform.service.RefreshTokenService;
import com.Nguyen.blogplatform.service.UserDetailsImpl;
import com.Nguyen.blogplatform.validation.annotation.WithRateLimitProtection;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")

public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${blog.app.refreshTokenCookieName}")
    private String refreshTokenCookieName;

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    @WithRateLimitProtection
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // logger.info("Registering user {} {} {}", signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());
        
        // AuthService now handles auto login after registration
        return authService.registerUser(signUpRequest);
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request) {

        String refreshTokenCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (refreshTokenCookieName.equals(cookie.getName())) {
                    refreshTokenCookie = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshTokenCookie == null || refreshTokenCookie.isEmpty()) {
            throw new TokenRefreshException(null, "Refresh token cookie is missing!");
        }

        final String refreshTokenCookieFinal = refreshTokenCookie;

        return refreshTokenService.findByToken(refreshTokenCookieFinal)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Tạo access token mới
                    String newAccessToken = jwtUtils.generateTokenFromUserId(user.getId(), user.getEmail());

                    // Xoay vòng refresh token (tạo cái mới / cập nhật cái cũ)
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

                    // Tạo cookies mới
                    ResponseCookie jwtCookie = ResponseCookie.from(jwtUtils.getJwtCookie(), newAccessToken)
                            .path("/")
                            .maxAge(7 * 24 * 60 * 60) // 7 ngày
                            .httpOnly(true)

                            .sameSite("Lax")
                            .build();

                    ResponseCookie refreshTokenCookieNew = refreshTokenService.generateRefreshTokenCookie(newRefreshToken.getToken());

                    // Trả lại access token + refresh token mới
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                            .header(HttpHeaders.SET_COOKIE, refreshTokenCookieNew.toString())
                            .body(new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new TokenRefreshException(refreshTokenCookieFinal,
                        "Refresh token is not in database!"));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);

        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie refreshTokenCookie = refreshTokenService.getCleanRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }




    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("No authentication found. Please log in.");
        }

        if (!authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication failed: " + authentication.getPrincipal());
        }

        Object principal = authentication.getPrincipal();
        logger.info("Principal type: " + (principal != null ? principal.getClass().getName() : "null"));
        logger.info("Principal: " + principal);

        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid authentication principal: " +
                            (principal != null ? principal.getClass().getName() : "null"));
        }

        try {
            // Rest of your existing code...
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() ->
                            new NotFoundException("User not found with id: " + userDetails.getId()));

            List<ERole> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("id", userDetails.getId());
            response.put("username", userDetails.getUsername());
            response.put("email", userDetails.getEmail());
            response.put("avatar", user.getAvatar());
            response.put("roles", roles);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in /me endpoint", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }


}
