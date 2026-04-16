package com.Nguyen.blogplatform.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtUtilsOptimizationTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    public void testTokenSizeOptimization() {
        // Test data
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String email = "test.user@example.com";

        // Generate optimized token (without email claim)
        String optimizedToken = jwtUtils.generateTokenFromUserId(userId, email);

        // Generate old-style token (with email claim) for comparison
        String oldStyleToken = generateOldStyleToken(userId, email);

        System.out.println("=== JWT Token Optimization Results ===");
        System.out.println("Old token (with email): " + oldStyleToken.length() + " characters");
        System.out.println("New token (optimized):  " + optimizedToken.length() + " characters");
        System.out.println("Size reduction: " + (oldStyleToken.length() - optimizedToken.length()) + " characters");
        System.out.println("Reduction percentage: " + 
            String.format("%.2f", ((oldStyleToken.length() - optimizedToken.length()) * 100.0 / oldStyleToken.length())) + "%");

        // Verify the optimized token is shorter
        assertTrue(optimizedToken.length() < oldStyleToken.length(), 
            "Optimized token should be shorter than old token");

        // Verify we can still extract userId
        String extractedUserId = jwtUtils.getUserIdFromJwtToken(optimizedToken);
        assertEquals(userId, extractedUserId, "Should be able to extract userId from optimized token");

        // Verify token validation works
        assertTrue(jwtUtils.validateJwtToken(optimizedToken), "Optimized token should be valid");

        System.out.println("\nOld token: " + oldStyleToken);
        System.out.println("\nNew token: " + optimizedToken);
    }

    @Test
    public void testTokenValidation() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String email = "user@test.com";

        String token = jwtUtils.generateTokenFromUserId(userId, email);

        // Should be valid
        assertTrue(jwtUtils.validateJwtToken(token));

        // Should extract correct userId
        assertEquals(userId, jwtUtils.getUserIdFromJwtToken(token));
    }

    private String generateOldStyleToken(String userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 180000); // 3 minutes

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
            "48226735186f1841e8d48215f709b70294cad4313ef992ac2d76665cd3a2946e"
        ));

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)  // Old style included email
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
}
