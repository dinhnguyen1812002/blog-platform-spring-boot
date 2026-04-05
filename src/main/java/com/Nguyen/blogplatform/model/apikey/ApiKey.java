package com.Nguyen.blogplatform.model.apikey;

import com.Nguyen.blogplatform.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing an API key for external platform integration.
 */
@Entity
@Table(name = "api_keys", indexes = {
    @Index(name = "idx_api_key", columnList = "apiKey", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    /**
     * The hashed version of the API secret for secure storage.
     */
    @Column(name = "api_secret_hash", nullable = false)
    private String apiSecretHash;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void revoke() {
        this.active = false;
    }
}
