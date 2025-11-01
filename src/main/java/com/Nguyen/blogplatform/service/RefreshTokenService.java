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

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
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


    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

//    @Transactional
//    public RefreshToken createRefreshToken(String userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
//
//        // Nếu đã tồn tại RefreshToken -> cập nhật lại token và expiry date
//        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
//                .orElse(new RefreshToken());
//
//        refreshToken.setUser(user);
//        refreshToken.setToken(generateSecureToken(32)); // ~43 ký tự
//        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
//
//        return refreshTokenRepository.save(refreshToken);
//    }

    @Transactional
    public RefreshToken createRefreshToken(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(generateSecureToken(32));

        return refreshTokenRepository.save(refreshToken);
    }

    private String generateSecureToken(int lengthBytes) {
        byte[] randomBytes = new byte[lengthBytes];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
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