package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.RoleRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    private final Logger logger = LoggerFactory.getLogger(DebugController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        logger.info("Found {} roles in database", roles.size());
        
        Map<String, Object> response = new HashMap<>();
        response.put("roles", roles);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsersWithRoles() {
        List<User> users = userRepository.findAll();
        
        List<Map<String, Object>> userDetails = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> details = new HashMap<>();
            details.put("id", user.getId());
            details.put("username", user.getUsername());
            details.put("email", user.getEmail());
            
            Set<Role> roles = user.getRoles();
            if (roles != null) {
                details.put("roles", roles);
                details.put("roleCount", roles.size());
            } else {
                details.put("roles", "null");
                details.put("roleCount", 0);
            }
            
            userDetails.add(details);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userDetails);
        response.put("count", users.size());
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/fix-roles")
//    @Transactional
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
    
    // @PostMapping("/decode-token")
    // public ResponseEntity<?> decodeToken(@RequestParam String token) {
    //     try {
    //         jwtUtils.debugJwtClaims(token);
            
    //         Map<String, Object> response = new HashMap<>();
    //         response.put("userId", jwtUtils.getUserIdFromJwtToken(token));
    //         response.put("username", jwtUtils.getUsernameFromJwtToken(token));
    //         response.put("roles", jwtUtils.getRolesFromJwtToken(token));
    //         response.put("valid", jwtUtils.validateJwtToken(token));
            
    //         return ResponseEntity.ok(response);
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body("Invalid token: " + e.getMessage());
    //     }
    // }
    
//    private void createRoleIfNotExists(String roleName) {
//        if (!roleRepository.findByName(roleName).isPresent()) {
//            logger.info("Creating missing role: {}", roleName);
//            Role role = new Role(roleName);
//            roleRepository.save(role);
//        }
//    }
//
//    private void fixRoleNames() {
//        // Fix roles without ROLE_ prefix
//        roleRepository.findAll().forEach(role -> {
//            String name = role.getName();
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
}