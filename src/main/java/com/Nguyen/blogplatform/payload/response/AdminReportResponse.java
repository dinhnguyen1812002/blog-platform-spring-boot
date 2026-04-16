package com.Nguyen.blogplatform.payload.response;

import com.Nguyen.blogplatform.Enum.ReportCategory;
import com.Nguyen.blogplatform.Enum.ReportStatus;

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
