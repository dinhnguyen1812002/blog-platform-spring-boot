package com.Nguyen.blogplatform.controller.Admin;

import com.Nguyen.blogplatform.Enum.ReportCategory;
import com.Nguyen.blogplatform.Enum.ReportStatus;
import com.Nguyen.blogplatform.payload.request.UpdateReportStatusRequest;
import com.Nguyen.blogplatform.payload.response.AdminReportResponse;
import com.Nguyen.blogplatform.service.auth.UserDetailsImpl;
import com.Nguyen.blogplatform.service.report.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin controller for managing article reports.
 * Provides moderation dashboard APIs.
 */
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin Report Management", description = "Admin APIs for managing article reports")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ADMIN')")
public class ReportManagementController {

    private final AdminReportService adminReportService;

    /**
     * Get all reports with optional filtering.
     *
     * @param status Filter by status (optional)
     * @param category Filter by category (optional)
     * @param pageable Pagination
     * @return Page of reports
     */
    @GetMapping
    @Operation(summary = "Get all reports", description = "List all reports with optional filtering by status and category")
    public ResponseEntity<Page<AdminReportResponse>> getAllReports(
            @Parameter(description = "Filter by status") @RequestParam(required = false) ReportStatus status,
            @Parameter(description = "Filter by category") @RequestParam(required = false) ReportCategory category,
            Pageable pageable) {
        
        return ResponseEntity.ok(adminReportService.getAllReports(status, category, pageable));
    }

    /**
     * Get pending reports for moderation queue.
     *
     * @param pageable Pagination
     * @return Page of pending reports
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending reports", description = "Get reports awaiting moderation")
    public ResponseEntity<Page<AdminReportResponse>> getPendingReports(Pageable pageable) {
        return ResponseEntity.ok(adminReportService.getPendingReports(pageable));
    }

    /**
     * Get a specific report with full details.
     *
     * @param reportId Report ID
     * @return Report details
     */
    @GetMapping("/{reportId}")
    @Operation(summary = "Get report details", description = "Get full details of a specific report")
    public ResponseEntity<AdminReportResponse> getReport(
            @Parameter(description = "Report ID") @PathVariable String reportId) {
        
        return ResponseEntity.ok(adminReportService.getReportById(reportId));
    }



    /**
     * Update report status.
     *
     * @param reportId Report ID
     * @param request Status update request
     * @param userDetails Admin user
     * @return Updated report
     */
    @PutMapping("/{reportId}/status")
    @Operation(summary = "Update report status", description = "Change report status (RESOLVED, DISMISSED, etc.)")
    public ResponseEntity<AdminReportResponse> updateReportStatus(
            @Parameter(description = "Report ID") @PathVariable String reportId,
            @Valid @RequestBody UpdateReportStatusRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        AdminReportResponse response = adminReportService.updateReportStatus(
                reportId, 
                request.status(), 
                request.adminNotes(), 
                userDetails.getId()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a report.
     *
     * @param reportId Report ID
     * @return Success message
     */
    @DeleteMapping("/{reportId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete report", description = "Permanently delete a report")
    public ResponseEntity<Void> deleteReport(
            @Parameter(description = "Report ID") @PathVariable String reportId) {
        
        adminReportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get report statistics for dashboard.
     *
     * @return Statistics map
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get report statistics", description = "Get statistics for admin dashboard")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(adminReportService.getReportStatistics());
    }
}
