package com.Nguyen.blogplatform.domain.auth.application;



import com.Nguyen.blogplatform.domain.auth.infrastructure.repository.JwtBlacklistRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.infrastructure.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to manage JWT token revocation.
 * Provides functionality to revoke tokens individually or in bulk.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRevocationService {

    private final JwtUtils jwtUtils;
    private final JwtBlacklistRepository jwtBlacklistRepository;
    private final RefreshTokenService refreshTokenService;

    /**
     * Revoke a specific JWT token by adding it to the blacklist.
     *
     * @param token JWT token to revoke
     * @param userId User ID who owns the token
     * @param reason Reason for revocation
     */
    @Transactional
    public void revokeToken(String token, String userId, String reason) {
        try {
            jwtUtils.addToBlacklist(token, userId, reason);
            log.info("Token revoked for user {}: reason={}", userId, reason);
        } catch (Exception e) {
            log.error("Failed to revoke token for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to revoke token", e);
        }
    }

    /**
     * Revoke the JWT token from the current HTTP request.
     * Typically used during logout.
     *
     * @param request HTTP request containing the JWT token
     * @param userId User ID
     */
    @Transactional
    public void revokeTokenFromRequest(HttpServletRequest request, String userId) {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null && !token.isEmpty()) {
                revokeToken(token, userId, "user_logout");
                log.info("Token from request revoked for user {}", userId);
            } else {
                log.warn("No token found in request for user {}", userId);
            }
        } catch (Exception e) {
            log.error("Error revoking token from request for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Revoke all active tokens for a specific user.
     * This is useful when a user changes their password or when a security breach is suspected.
     *
     * @param userId User ID whose tokens should be revoked
     * @param reason Reason for bulk revocation
     */
    @Transactional
    public void revokeAllUserTokens(String userId, String reason) {
        try {
            // Note: Since JWT tokens are stateless, we can't enumerate all active tokens.
            // However, we can:
            // 1. Delete all refresh tokens for the user
            // 2. Log the revocation event
            // The user will need to re-authenticate to get new tokens
            
            refreshTokenService.deleteByUserId(userId);
            log.info("All refresh tokens revoked for user {}: reason={}", userId, reason);
            
            // In a more advanced implementation, you could maintain a token registry
            // to track all issued tokens per user
        } catch (Exception e) {
            log.error("Failed to revoke all tokens for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to revoke all user tokens", e);
        }
    }

    /**
     * Check if a token has been revoked.
     *
     * @param token JWT token
     * @return true if token is revoked/blacklisted
     */
    public boolean isTokenRevoked(String token) {
        return jwtUtils.isTokenBlacklisted(token);
    }

    /**
     * Extract JWT token from HTTP request.
     * Checks both cookies and Authorization header.
     *
     * @param request HTTP request
     * @return JWT token or null if not found
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // First try to get token from cookie
        String token = jwtUtils.getJwtFromCookies(request);
        
        // If not found in cookie, try Authorization header
        if (token == null) {
            token = jwtUtils.getJwtFromHeader(request);
        }
        
        return token;
    }

    /**
     * Get statistics about blacklisted tokens.
     *
     * @return String containing statistics
     */
    public String getBlacklistStats() {
        long totalBlacklisted = jwtBlacklistRepository.count();
        return String.format("Total blacklisted tokens: %d", totalBlacklisted);
    }
}
