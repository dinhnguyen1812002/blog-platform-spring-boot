package com.Nguyen.blogplatform.payload.response;

import com.Nguyen.blogplatform.Enum.ReportCategory;
import com.Nguyen.blogplatform.Enum.ReportStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for article report (user-facing).
 */
public record ReportResponse(
        String id,
        String postId,
        String postTitle,
        String postSlug,
        ReportCategory category,
        String description,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
