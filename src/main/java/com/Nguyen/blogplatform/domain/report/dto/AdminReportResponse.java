package com.Nguyen.blogplatform.domain.report.dto;




import com.Nguyen.blogplatform.shared.enums.ReportCategory;
import com.Nguyen.blogplatform.shared.enums.ReportStatus;
import java.time.LocalDateTime;

/**
 * Response DTO for article report (admin-facing with full details).
 */
public record AdminReportResponse(
        String id,
        String postId,
        String postTitle,
        String postSlug,
        String postAuthor,
        String reporterId,
        String reporterUsername,
        String reporterEmail,
        ReportCategory category,
        String description,
        ReportStatus status,
        String adminNotes,
        String reviewedBy,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
