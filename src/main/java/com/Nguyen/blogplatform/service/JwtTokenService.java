package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.security.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    
//    private final JwtUtils jwtUtils;
//
//    /**
//     * Extract user ID from JWT token
//     */
//    public String extractUserId(String token) {
//        return jwtUtils.getUserIdFromJwtToken(token);
//    }
//
//    /**
//     * Extract username from JWT token
//     */
//    public String extractUsername(String token) {
//        return jwtUtils.getUsernameFromJwtToken(token);
//    }
//
//    /**
//     * Extract roles from JWT token as a list
//     */
//    public List<String> extractRoles(String token) {
//        String rolesString = jwtUtils.getRolesFromJwtToken(token);
//        if (rolesString == null || rolesString.isEmpty()) {
//            return List.of();
//        }
//        return Arrays.asList(rolesString.split(","));
//    }
//
//    /**
//     * Check if user has a specific role
//     */
//    public boolean hasRole(String token, String role) {
//        List<String> roles = extractRoles(token);
//        return roles.contains(role) || roles.contains("ROLE_" + role);
//    }
//
//    /**
//     * Check if user has admin role
//     */
//    public boolean isAdmin(String token) {
//        return hasRole(token, "ADMIN") || hasRole(token, "ROLE_ADMIN");
//    }
//
//    /**
//     * Check if user has author role
//     */
//    public boolean isAuthor(String token) {
//        return hasRole(token, "AUTHOR") || hasRole(token, "ROLE_AUTHOR");
//    }
//
//    /**
//     * Check if user has user role
//     */
//    public boolean isUser(String token) {
//        return hasRole(token, "USER") || hasRole(token, "ROLE_USER");
//    }
//
//    /**
//     * Get all claims from JWT token
//     */
////    public Claims getAllClaims(String token) {
////        return jwtUtils.getAllClaimsFromJwtToken(token);
////    }
//
//    /**
//     * Validate JWT token
//     */
//    public boolean isTokenValid(String token) {
//        return jwtUtils.validateJwtToken(token);
//    }
//
//    /**
//     * Extract token information as a formatted string
//     */
//    public String getTokenInfo(String token) {
//        if (!isTokenValid(token)) {
//            return "Invalid token";
//        }
//
//        String userId = extractUserId(token);
//        String username = extractUsername(token);
//        List<String> roles = extractRoles(token);
//
//        return String.format("User: %s (ID: %s), Roles: %s", username, userId, roles);
//    }
}
