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
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Get user from database to check roles
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        logger.info("User roles from database: {}", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));

        List<ERole> rolesFromDatabase = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Create new refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        // Generate cookies
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(authentication);
        ResponseCookie refreshTokenCookie = refreshTokenService.generateRefreshTokenCookie(refreshToken.getToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new JwtResponse(
                        jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        rolesFromDatabase.stream().map(ERole::name).collect(Collectors.toList())
                ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        // logger.info("Registering user {} {} {}", signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());
        return authService.registerUser(signUpRequest);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUserId(user.getId(), user.getEmail());

                    // Generate cookies
                    ResponseCookie jwtCookie = ResponseCookie.from(jwtUtils.getJwtCookie(), token)
                            .path("/")
                            .maxAge(24 * 60 * 60)
                            .httpOnly(true)
                            .secure(true)
                            .sameSite("Lax")
                            .build();

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                            .body(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
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
        System.out.println("Roles from userDetails: " + roles);
        System.out.println("Roles from database: " + user.getRoles().stream()
                .map(Role::getName)
                .toList());

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("id", userDetails.getId());
        response.put("username", userDetails.getUsername());
//        response.put("email", userDetails.getEmail());
        response.put("role", roles); // Use roles from userDetails for consistency

        return ResponseEntity.ok(response);
    }

//    @PostMapping("/fix-roles")
//    @Transactional
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<?> fixRoles() {
//        logger.info("Starting role fix process");
//
//        // 1. Ensure roles exist with ROLE_ prefix
//        createRoleIfNotExists("ROLE_USER");
//        createRoleIfNotExists("ROLE_AUTHOR");
//        createRoleIfNotExists("ROLE_ADMIN");
//
//        // 2. Fix existing roles without prefix
//        fixRoleNames();
//
//        // 3. Assign roles to users without roles
//        int updatedUsers = assignRolesToUsersWithoutRoles();
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Roles fixed successfully");
//        response.put("usersUpdated", updatedUsers);
//        return ResponseEntity.ok(response);
//    }
//
//    private void createRoleIfNotExists(ERole roleName) {
//        if (!roleRepository.findByName(roleName).isPresent()) {
//            logger.info("Creating missing role: {}", roleName);
//            Role role = new Role(roleName);
//            roleRepository.save(role);
//        }
//    }

//    private void fixRoleNames() {
//        // Fix roles without ROLE_ prefix
//        roleRepository.findAll().forEach(role -> {
//            ERole name = role.getName();
//            if (!name.startsWith("ROLE_")) {
//                logger.info("Fixing role name: {} -> ROLE_{}", name, name);
//                role.setName("ROLE_" + name);
//                roleRepository.save(role);
//            }
//        });
//    }
//
//    private int assignRolesToUsersWithoutRoles() {
//        int count = 0;
//        Role userRole = roleRepository.findByName("ROLE_USER")
//                .orElseGet(() -> {
//                    Role newRole = new Role("ROLE_USER");
//                    return roleRepository.save(newRole);
//                });
//
//        for (User user : userRepository.findAll()) {
//            if (user.getRoles() == null || user.getRoles().isEmpty()) {
//                logger.info("Assigning ROLE_USER to user: {}", user.getUsername());
//                Set<Role> roles = new HashSet<>();
//                roles.add(userRole);
//                user.setRoles(roles);
//                userRepository.save(user);
//                count++;
//            }
//        }
//        return count;
//    }

//    @GetMapping("/me")
//    public ResponseEntity<?> getUser (@AuthenticationPrincipal AuthenticationPrincipal principal) {
//        if(principal == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
//        }
//        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
//        List<String> roles = userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .toList();
//
//        return ResponseEntity.ok(principal);
//
//    }

}
