package com.Nguyen.blogplatform.domain.report.dto;




import com.Nguyen.blogplatform.shared.enums.ReportCategory;
import com.Nguyen.blogplatform.shared.enums.ReportStatus;
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
