package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.payload.request.LoginRequest;
import com.Nguyen.blogplatform.payload.request.SignupRequest;
import com.Nguyen.blogplatform.payload.response.JwtResponse;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.service.AuthService;
import com.Nguyen.blogplatform.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")

public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        return authService.authenticateUser(loginRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        logger.info("Registering user {} {} {}", signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());
        return authService.registerUser(signUpRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        return authService.logoutUser();
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", userDetails.getId());
        response.put("username", userDetails.getUsername());
        response.put("email", userDetails.getEmail());

        return ResponseEntity.ok(response);
    }

}
