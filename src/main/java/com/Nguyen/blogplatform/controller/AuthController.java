package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.LoginRequest;
import com.Nguyen.blogplatform.payload.request.SignupRequest;
import com.Nguyen.blogplatform.payload.response.JwtResponse;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.repository.RoleRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import com.Nguyen.blogplatform.service.AuthService;
import com.Nguyen.blogplatform.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//        Authentication authentication1  = SecurityContextHolder.getContext().getAuthentication();

        // Get user from database to check roles
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        logger.info("User roles from database: {}", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
//        List<String> rolesFromDatabase = user.getRoles().stream()
//                .map(role -> role.getName().name())
//                .collect(Collectors.toList());
//        System.out.println("role: " + rolesFromDatabase);
        List<ERole> rolesFromDatabase = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

//        return ResponseEntity.ok(new JwtResponse(jwt,
//                userDetails.getId(),
//                userDetails.getUsername(),
//                userDetails.getEmail(),
//                roles));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(auth);
        String jwtToken = jwtUtils.generateJwtToken(auth);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new JwtResponse(
                        jwtToken,
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

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        return authService.logoutUser();
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