package com.Nguyen.blogplatform.model;

import com.Nguyen.blogplatform.Enum.EDeliveryChannel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification_preferences",
    indexes = {
        @Index(name = "idx_prefs_user_id", columnList = "user_id"),
        @Index(name = "idx_prefs_user_channel", columnList = "user_id, channel", unique = true)
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private EDeliveryChannel channel;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "digest_mode", length = 20)
    @Builder.Default
    private String digestMode = "immediate";

    @Column(name = "quiet_hours_start")
    private Integer quietHoursStart;

    @Column(name = "quiet_hours_end")
    private Integer quietHoursEnd;

    @Column(name = "email_address", length = 255)
    private String emailAddress;

    @Column(name = "push_token", length = 500)
    private String pushToken;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
