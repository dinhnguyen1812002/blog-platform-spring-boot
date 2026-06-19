package com.Nguyen.blogplatform.domain.report.domain.model;



import com.Nguyen.blogplatform.domain.post.domain.model.Category;
import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.shared.enums.ReportCategory;
import com.Nguyen.blogplatform.shared.enums.ReportStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a user report for an inappropriate or problematic article.
 * Tracks the report lifecycle from submission to resolution.
 */
@Entity
@Table(
    name = "article_reports",
    indexes = {
        @Index(name = "idx_report_post", columnList = "post_id"),
        @Index(name = "idx_report_user", columnList = "reporter_id"),
        @Index(name = "idx_report_status", columnList = "status"),
        @Index(name = "idx_report_category", columnList = "category"),
        @Index(name = "idx_report_created", columnList = "createdAt")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_post_report",
            columnNames = {"reporter_id", "post_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * The post being reported
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @NotNull(message = "Post is required")
    private Post post;

    /**
     * The user who submitted the report
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @NotNull(message = "Reporter is required")
    private User reporter;

    /**
     * Category of the report (spam, inappropriate, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    @NotNull(message = "Report category is required")
    private ReportCategory category;

    /**
     * Detailed explanation from the reporter (required for "OTHER" category)
     */
    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Current status of the report
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    /**
     * Admin/moderator notes about the report resolution
     */
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Admin notes must not exceed 2000 characters")
    private String adminNotes;

    /**
     * ID of the admin/moderator who reviewed the report
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    /**
     * Timestamp when the report was reviewed
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * Timestamp when the report was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the report was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Check if this report is still pending review
     */
    public boolean isPending() {
        return status == ReportStatus.PENDING;
    }

    /**
     * Check if this report has been resolved
     */
    public boolean isResolved() {
        return status == ReportStatus.RESOLVED;
    }

    /**
     * Check if this report has been dismissed
     */
    public boolean isDismissed() {
        return status == ReportStatus.DISMISSED;
    }
}
