package com.Nguyen.blogplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

/**
 * Entity to store revoked JWT tokens in a blacklist.
 * Used to invalidate tokens before their natural expiration.
 */
@Entity
@Table(
    name = "jwt_blacklist",
    indexes = {
        @Index(name = "idx_jwt_blacklist_token_hash", columnList = "tokenHash", unique = true),
        @Index(name = "idx_jwt_blacklist_expiry_date", columnList = "expiryDate")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * SHA-256 hash of the JWT token.
     * We store the hash instead of the full token for security and space efficiency.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /**
     * The expiration date of the original JWT token.
     * Used to clean up expired entries from the blacklist.
     */
    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    /**
     * Timestamp when the token was revoked.
     */
    @CreatedDate
    @Column(name = "revoked_at", nullable = false, updatable = false)
    private Instant revokedAt;

    /**
     * Reason for revocation (e.g., "user_logout", "security_breach", "password_change").
     */
    @Column(name = "reason", length = 100)
    private String reason;

    /**
     * ID of the user who owned this token.
     * Useful for bulk revocation of all user tokens.
     */
    @Column(name = "user_id", length = 36)
    private String userId;
}
