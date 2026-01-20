package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.RefreshToken;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.payload.request.LoginRequest;
import com.Nguyen.blogplatform.payload.request.SignupRequest;
import com.Nguyen.blogplatform.payload.response.JwtResponse;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.repository.RoleRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public ResponseEntity<?> authenticateUser(@Valid LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()
                || !loginRequest.getEmail().matches("^(.+)@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        if (loginRequest.getPassword() == null
                || loginRequest.getPassword().isEmpty()
                || loginRequest.getPassword().length() < 6
                || loginRequest.getPassword().length() > 40) {
            return ResponseEntity.badRequest().body("Invalid password format");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserDetails userDetailss =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("User details abbc: " + userDetailss.getAuthorities());


        String jwtToken = jwtUtils.generateJwtToken(authentication);
//        String refreshToken = jwtUtils.generateRefreshToken(authentication);
        jwtUtils.debugJwtClaims(jwtToken);

        // Create JwtResponse with full user info
        List<String> roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        if(roles.isEmpty()){
            System.out.println("Role bị null");
        } else {
            System.out.println("Role: "+ roles);
        }

        JwtResponse jwtResponse = new JwtResponse(
                jwtToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getSlug(),
                userDetails.getAvatar(),
                roles
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(authentication);
        ResponseCookie refreshTokenCookie = refreshTokenService.generateRefreshTokenCookie(refreshToken.getToken());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        responseHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(jwtResponse);
    }

    public ResponseEntity<?> registerUser(@Valid SignupRequest signUpRequest) {
        String err = "Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character, and no whitespace";
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new NotFoundException("Error: Username is already taken!"));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new NotFoundException("Error: Email is already in use!"));
        }
        if (!validatePassword(signUpRequest.getPassword())) {
            return ResponseEntity.badRequest().body(err);
        }
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER )
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ROLE_ADMIN":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN
                                )
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "ROLE_AUTHOR":
                        Role modRole = roleRepository.findByName(ERole.ROLE_AUTHOR)
                                .orElseThrow(() -> new NotFoundException("Error: Role is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new NotFoundException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // AUTO LOGIN: Authenticate the newly registered user
        try {
            // Create authentication token with the original password (before encoding)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signUpRequest.getEmail(), signUpRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

            // Generate cookies
            ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(authentication);
            ResponseCookie refreshTokenCookie = refreshTokenService.generateRefreshTokenCookie(refreshToken.getToken());

            // Prepare role names for response
            List<String> roleNames = roles.stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList());

            // Create JWT response with auto login
            JwtResponse jwtResponse = new JwtResponse(
                    jwt,
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getSlug(),
                    roleNames,
                    "Registration successful! You have been automatically logged in."
            );

            // Set response headers with cookies
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
            responseHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(jwtResponse);

        } catch (Exception e) {
            // If auto login fails, return user info without JWT (fallback)
            System.err.println("Auto login failed after registration: " + e.getMessage());
            
            List<ERole> roleNames = roles.stream()
                    .map(Role::getName)
                    .toList();

            UserResponse userResponse = new UserResponse(savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getAvatar(),
                    roleNames);

            return ResponseEntity.ok(userResponse);
        }
    }

    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

    private boolean validatePassword(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$");
    }
}