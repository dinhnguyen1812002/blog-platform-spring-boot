package com.Nguyen.blogplatform.domain.report.dto;



import com.Nguyen.blogplatform.domain.post.domain.model.Category;
import com.Nguyen.blogplatform.shared.enums.ReportCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for submitting an article report.
 */
public record ReportRequest(
        /**
         * Category of the violation
         */
        @NotNull(message = "Report category is required")
        ReportCategory category,

        /**
         * Detailed explanation (required for OTHER category, max 2000 chars)
         */
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description
) {
    /**
     * Validate that description is provided when category is OTHER.
     *
     * @return true if validation passes
     */
    public boolean isValid() {
        if (category == ReportCategory.OTHER) {
            return description != null && !description.trim().isEmpty();
        }
        return true;
    }
}
