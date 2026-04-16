package com.Nguyen.blogplatform.payload.request;

import com.Nguyen.blogplatform.Enum.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating report status.
 */
public record UpdateReportStatusRequest(
        /**
         * New status for the report
         */
        @NotNull(message = "Status is required")
        ReportStatus status,

        /**
         * Admin notes about the decision (optional, max 2000 chars)
         */
        @Size(max = 2000, message = "Admin notes must not exceed 2000 characters")
        String adminNotes
) {
}
