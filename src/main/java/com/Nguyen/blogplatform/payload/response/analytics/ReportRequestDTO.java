package com.Nguyen.blogplatform.payload.response.analytics;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReportRequestDTO(
    @NotNull(message = "Report type is required")
    ReportType reportType,
    
    @NotNull(message = "Year is required")
    @Min(value = 2020, message = "Year must be at least 2020")
    @Max(value = 2100, message = "Year must not exceed 2100")
    Integer year,
    
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    Integer month,
    
    @Min(value = 1, message = "Quarter must be between 1 and 4")
    @Max(value = 4, message = "Quarter must be between 1 and 4")
    Integer quarter,
    
    @NotNull(message = "Export format is required")
    ExportFormat exportFormat
) {
    public enum ReportType {
        MONTH, QUARTER, YEAR
    }
    
    public enum ExportFormat {
        PDF, EXCEL
    }
}