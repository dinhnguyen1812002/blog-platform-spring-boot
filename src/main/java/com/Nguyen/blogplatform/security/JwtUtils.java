package com.Nguyen.blogplatform.security;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.Nguyen.blogplatform.model.JwtBlacklist;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.JwtBlacklistRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.service.auth.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final UserRepository userRepository;
    private final JwtBlacklistRepository jwtBlacklistRepository;

    @Value("${blog.app.jwtSecret}")
    private String jwtSecret;

    @Value("${blog.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Getter
    @Value("${blog.app.jwtCookieName}")
    private String jwtCookie;

    @Value("${blog.app.cookieSecure:false}")
    private boolean cookieSecure;

    /**
     * Generate JWT token from Authentication object
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return generateTokenFromUserId(userPrincipal.getId(), userPrincipal.getEmail());
    }

    /**
     * Generate JWT token from userId and email
     * Optimized: Only stores userId in token, email is fetched from DB when needed
     * Enhanced: Includes JTI (JWT ID) claim for token revocation support
     */
    public String generateTokenFromUserId(String userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        // Generate unique JWT ID for revocation support
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)  // JTI claim for token revocation
                .subject(userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate JWT cookie from Authentication
     */
    public ResponseCookie generateJwtCookie(Authentication authentication) {
        String jwt = generateJwtToken(authentication);
        return generateCookieFromToken(jwt);
    }

    /**
     * Generate JWT cookie from token string
     */
    public ResponseCookie generateCookieFromToken(String token) {
        return ResponseCookie.from(jwtCookie, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(24 * 60 * 60) // 24 hours
                .sameSite("Lax")
                .build();
    }

    /**
     * Get JWT token from cookies in request
     */
    public String getJwtFromCookies(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, jwtCookie))
                .map(Cookie::getValue)
                .orElse(null);
    }

    /**
     * Get JWT token from Authorization header (Bearer token)
     */
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Generate clean cookie to logout
     */
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract user ID from JWT token
     */
    public String getUserIdFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Extract email from JWT token
     * @deprecated Email is no longer stored in JWT. Use UserService to fetch email by userId.
     */
    @Deprecated
    public String getEmailFromJwtToken(String token) {
        logger.warn("getEmailFromJwtToken is deprecated. Email is not stored in JWT anymore.");
        return null;
    }

    /**
     * Extract all claims from JWT token
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Check if a token is blacklisted
     * 
     * @param token JWT token
     * @return true if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenHash = hashToken(token);
            return jwtBlacklistRepository.existsByTokenHash(tokenHash);
        } catch (Exception e) {
            logger.error("Error checking token blacklist: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Add a token to the blacklist
     * 
     * @param token JWT token to blacklist
     * @param userId User ID who owned the token
     * @param reason Reason for revocation
     */
    public void addToBlacklist(String token, String userId, String reason) {
        try {
            String tokenHash = hashToken(token);
            
            // Extract expiration date from token
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            Date expiration = claims.getExpiration();
            
            JwtBlacklist blacklistEntry = JwtBlacklist.builder()
                    .tokenHash(tokenHash)
                    .expiryDate(expiration.toInstant())
                    .userId(userId)
                    .reason(reason)
                    .build();
            
            jwtBlacklistRepository.save(blacklistEntry);
            logger.info("Token blacklisted for user: {}, reason: {}", userId, reason);
        } catch (Exception e) {
            logger.error("Error adding token to blacklist: {}", e.getMessage());
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    /**
     * Hash a JWT token using SHA-256
     * 
     * @param token JWT token
     * @return SHA-256 hash as hex string
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Validate JWT token
     * Enhanced: Checks token blacklist before validation
     */
    public boolean validateJwtToken(String authToken) {
        try {
            // First check if token is blacklisted
            if (isTokenBlacklisted(authToken)) {
                logger.warn("JWT token is blacklisted");
                return false;
            }
            
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("JWT token validation error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Refresh token (generate new token with same user info)
     * Fetches user data from database to regenerate token
     */
    public String refreshToken(String oldToken) {
        try {
            String userId = getUserIdFromJwtToken(oldToken);
            
            // Fetch user from database to get current email
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
            return generateTokenFromUserId(userId, user.getEmail());
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage());
            throw new RuntimeException("Cannot refresh token", e);
        }
    }
}