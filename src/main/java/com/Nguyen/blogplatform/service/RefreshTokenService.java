package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.TokenRefreshException;
import com.Nguyen.blogplatform.model.RefreshToken;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.RefreshTokenRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${blog.app.refreshTokenExpirationMs}")
    private Long refreshTokenDurationMs;
    
    @Value("${blog.app.refreshTokenCookieName}")
    private String refreshTokenCookieName;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(String userId) {
        RefreshToken refreshToken = new RefreshToken();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Check if user already has a refresh token
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        // Delete existing token
        existingToken.ifPresent(token -> refreshTokenRepository.delete(token));

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public void deleteByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }
    
    public ResponseCookie generateRefreshTokenCookie(String token) {
        return ResponseCookie.from(refreshTokenCookieName, token)
                .path("/api/v1/auth")
                .maxAge(refreshTokenDurationMs / 1000) // Convert to seconds
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .build();
    }
    
    public ResponseCookie getCleanRefreshTokenCookie() {
        return ResponseCookie.from(refreshTokenCookieName, "")
                .path("/api/v1/auth")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .build();
    }
}