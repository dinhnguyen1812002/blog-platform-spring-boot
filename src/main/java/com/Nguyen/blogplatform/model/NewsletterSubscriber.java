package com.Nguyen.blogplatform.model;

import com.Nguyen.blogplatform.Enum.ENewsletterStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "newsletter_subscribers",
    indexes = {
        @Index(name = "idx_newsletter_email", columnList = "email", unique = true),
        @Index(name = "idx_newsletter_status", columnList = "status"),
        @Index(name = "idx_newsletter_token", columnList = "unsubscribe_token"),
        @Index(name = "idx_newsletter_confirm_token", columnList = "confirmation_token"),
        @Index(name = "idx_newsletter_created_at", columnList = "created_at")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ENewsletterStatus status = ENewsletterStatus.PENDING;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "signup_ip", length = 45)
    private String signupIp;

    @Column(name = "confirmed_ip", length = 45)
    private String confirmedIp;

    @Column(name = "unsubscribe_token", nullable = false, unique = true, length = 36)
    @Builder.Default
    private String unsubscribeToken = UUID.randomUUID().toString();

    @Column(name = "confirmation_token", unique = true, length = 36)
    private String confirmationToken;

    @Column(name = "confirmation_token_expires_at")
    private LocalDateTime confirmationTokenExpiresAt;

    @Column(name = "preferences", columnDefinition = "TEXT")
    private String preferences;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    @Column(name = "last_sent_at")
    private LocalDateTime lastSentAt;

    @Column(name = "bounce_count")
    @Builder.Default
    private Integer bounceCount = 0;

    @Column(name = "last_bounce_at")
    private LocalDateTime lastBounceAt;

    @Column(name = "gdpr_consent", nullable = false)
    @Builder.Default
    private Boolean gdprConsent = false;

    @Column(name = "gdpr_consent_at")
    private LocalDateTime gdprConsentAt;

    @PrePersist
    public void prePersist() {
        if (this.unsubscribeToken == null) {
            this.unsubscribeToken = UUID.randomUUID().toString();
        }
        if (this.status == ENewsletterStatus.PENDING && this.confirmationToken == null) {
            this.confirmationToken = UUID.randomUUID().toString();
            this.confirmationTokenExpiresAt = LocalDateTime.now().plusHours(24);
        }
    }
}
