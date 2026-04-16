package com.Nguyen.blogplatform.controller.report;

import com.Nguyen.blogplatform.payload.request.ReportRequest;
import com.Nguyen.blogplatform.payload.response.ReportResponse;
import com.Nguyen.blogplatform.service.auth.UserDetailsImpl;
import com.Nguyen.blogplatform.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user-facing report endpoints.
 * Allows authenticated users to report inappropriate content.
 */
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Article Reports", description = "APIs for reporting inappropriate articles")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    /**
     * Submit a report for a post.
     * 
     * @param postId ID of the post to report
     * @param request Report details
     * @param userDetails Authenticated user
     * @return Created report
     */
    @PostMapping("/{postId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Report a post",
        description = "Submit a report for inappropriate content. Rate limited to 5 reports per hour."
    )
    public ResponseEntity<ReportResponse> reportPost(
            @Parameter(description = "ID of the post to report") @PathVariable String postId,
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        ReportResponse response = reportService.submitReport(postId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get details of a specific report (for the reporter).
     *
     * @param reportId ID of the report
     * @param userDetails Authenticated user
     * @return Report details
     */
    @GetMapping("/reports/{reportId}")
    @Operation(
        summary = "Get report details",
        description = "View details of a report you submitted"
    )
    public ResponseEntity<ReportResponse> getReport(
            @Parameter(description = "ID of the report") @PathVariable String reportId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        ReportResponse response = reportService.getReportById(reportId, userDetails.getId());
        return ResponseEntity.ok(response);
    }
}
