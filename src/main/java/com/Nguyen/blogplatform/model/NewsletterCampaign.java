package com.Nguyen.blogplatform.model;

import com.Nguyen.blogplatform.Enum.ECampaignStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "newsletter_campaigns",
    indexes = {
        @Index(name = "idx_campaign_status", columnList = "status"),
        @Index(name = "idx_campaign_scheduled_at", columnList = "scheduled_at"),
        @Index(name = "idx_campaign_created_at", columnList = "created_at")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Lob
    @Column(name = "html_content", nullable = false, columnDefinition = "LONGTEXT")
    private String htmlContent;

    @Lob
    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "from_name", length = 100)
    private String fromName;

    @Column(name = "from_email", length = 255)
    private String fromEmail;

    @Column(name = "reply_to", length = 255)
    private String replyTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ECampaignStatus status = ECampaignStatus.DRAFT;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "target_segment", length = 500)
    private String targetSegment;

    @Column(name = "target_tags", length = 500)
    private String targetTags;

    @Column(name = "recipient_count")
    private Long recipientCount;

    @Column(name = "sent_count")
    @Builder.Default
    private Long sentCount = 0L;

    @Column(name = "opened_count")
    @Builder.Default
    private Long openedCount = 0L;

    @Column(name = "clicked_count")
    @Builder.Default
    private Long clickedCount = 0L;

    @Column(name = "bounced_count")
    @Builder.Default
    private Long bouncedCount = 0L;

    @Column(name = "unsubscribed_count")
    @Builder.Default
    private Long unsubscribedCount = 0L;

    @Column(name = "complained_count")
    @Builder.Default
    private Long complainedCount = 0L;

    @Column(name = "batch_size")
    @Builder.Default
    private Integer batchSize = 100;

    @Column(name = "send_interval_seconds")
    @Builder.Default
    private Integer sendIntervalSeconds = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "utm_source", length = 100)
    private String utmSource;

    @Column(name = "utm_medium", length = 100)
    private String utmMedium;

    @Column(name = "utm_campaign", length = 100)
    private String utmCampaign;
}
