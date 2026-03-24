package com.Nguyen.blogplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs",
    indexes = {
        @Index(name = "idx_email_campaign", columnList = "campaign_id"),
        @Index(name = "idx_email_subscriber", columnList = "subscriber_id"),
        @Index(name = "idx_email_status", columnList = "status"),
        @Index(name = "idx_email_created_at", columnList = "created_at"),
        @Index(name = "idx_email_message_id", columnList = "external_message_id")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "campaign_id", length = 36)
    private String campaignId;

    @Column(name = "subscriber_id", nullable = false, length = 36)
    private String subscriberId;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Column(name = "subject", length = 500)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;

    @Column(name = "external_message_id", length = 200)
    private String externalMessageId;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "bounce_reason", length = 500)
    private String bounceReason;

    @Column(name = "bounce_type", length = 50)
    private String bounceType;

    @Column(name = "complaint_type", length = 50)
    private String complaintType;

    public enum EmailStatus {
        PENDING,
        QUEUED,
        SENDING,
        SENT,
        DELIVERED,
        OPENED,
        CLICKED,
        BOUNCED,
        COMPLAINED,
        FAILED,
        RETRYING
    }
}
