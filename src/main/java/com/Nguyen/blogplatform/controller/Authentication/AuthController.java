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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
            @CookieValue(name = "${blog.app.refreshTokenCookieName}", required = false) String refreshTokenCookie) {

        if (refreshTokenCookie == null || refreshTokenCookie.isEmpty()) {
            throw new TokenRefreshException(null, "Refresh token cookie is missing!");
        }

        return refreshTokenService.findByToken(refreshTokenCookie)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Tạo access token mới
                    String newAccessToken = jwtUtils.generateTokenFromUserId(user.getId(), user.getEmail());

                    // Xoay vòng refresh token (xóa cái cũ, tạo cái mới)
                    refreshTokenService.deleteByUserId(user.getId());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

                    // Tạo cookies mới
                    ResponseCookie jwtCookie = ResponseCookie.from(jwtUtils.getJwtCookie(), newAccessToken)
                            .path("/")
                            .maxAge(7 * 24 * 60 * 60) // 7 ngày
                            .httpOnly(true)
                            .secure(true)
                            .sameSite("Lax")
                            .build();

                    ResponseCookie refreshTokenCookieNew = refreshTokenService.generateRefreshTokenCookie(newRefreshToken.getToken());

                    // Trả lại access token + refresh token mới
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                            .header(HttpHeaders.SET_COOKIE, refreshTokenCookieNew.toString())
                            .body(new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new TokenRefreshException(refreshTokenCookie,
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
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
//        if (userDetails == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
//        }

        // Fetch user from database (optional, only if additional data is needed)
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userDetails.getId()));

        // Get roles from userDetails (preferred, as it's from the authenticated JWT)
        List<ERole> roles = user.getRoles().stream()
                .map(Role::getName) // Map Role to its name (String)
                .collect(Collectors.toList());

        // Debug roles

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("id", userDetails.getId());
        response.put("username", userDetails.getUsername());
        response.put("email", userDetails.getEmail());
        response.put("avatar",userDetails.getAvatar());
//        response.put("")
        response.put("role", roles); // Use roles from userDetails for consistency

        return ResponseEntity.ok(response);
    }

}
