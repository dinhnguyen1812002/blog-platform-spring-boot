package com.Nguyen.blogplatform.controller.Admin;

import com.Nguyen.blogplatform.payload.response.ApiResponse;
import com.Nguyen.blogplatform.payload.response.analytics.*;
import com.Nguyen.blogplatform.service.analytics.AnalyticsService;
import com.Nguyen.blogplatform.service.export.AnalyticsExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor

@Tag(name = "Admin Analytics", description = "Analytics endpoints for Admin role")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsExportService analyticsExportService;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary", description = "Returns summary statistics including total views, active users, new posts, and total likes")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {
        DashboardSummaryDTO summary = analyticsService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved successfully", summary));
    }

    @GetMapping("/growth/monthly")
    @Operation(summary = "Get monthly growth analytics", description = "Returns monthly growth statistics for the specified year")
    public ResponseEntity<ApiResponse<List<MonthlyGrowthDTO>>> getMonthlyGrowth(
            @Parameter(description = "Year to get analytics for") @RequestParam int year) {
        List<MonthlyGrowthDTO> growth = analyticsService.getMonthlyGrowth(year);
        return ResponseEntity.ok(ApiResponse.success("Monthly growth analytics retrieved successfully", growth));
    }

    @GetMapping("/users/monthly")
    @Operation(summary = "Get new users per month", description = "Returns count of new users grouped by month for the specified year")
    public ResponseEntity<ApiResponse<List<MonthlyStatDTO>>> getNewUsersPerMonth(
            @Parameter(description = "Year to get analytics for") @RequestParam int year) {
        List<MonthlyStatDTO> stats = analyticsService.getNewUsersPerMonth(year);
        return ResponseEntity.ok(ApiResponse.success("New users per month retrieved successfully", stats));
    }

    @GetMapping("/posts/monthly")
    @Operation(summary = "Get post growth per month", description = "Returns count of new posts grouped by month for the specified year")
    public ResponseEntity<ApiResponse<List<MonthlyStatDTO>>> getPostGrowthPerMonth(
            @Parameter(description = "Year to get analytics for") @RequestParam int year) {
        List<MonthlyStatDTO> stats = analyticsService.getPostGrowthPerMonth(year);
        return ResponseEntity.ok(ApiResponse.success("Post growth per month retrieved successfully", stats));
    }

    @GetMapping("/posts/top-viewed")
    @Operation(summary = "Get most viewed posts", description = "Returns top N posts ranked by view count")
    public ResponseEntity<ApiResponse<List<TopPostDTO>>> getMostViewedPosts(
            @Parameter(description = "Number of top posts to return") @RequestParam(defaultValue = "10") int limit) {
        List<TopPostDTO> posts = analyticsService.getMostViewedPosts(limit);
        return ResponseEntity.ok(ApiResponse.success("Most viewed posts retrieved successfully", posts));
    }

    @GetMapping("/posts/top-liked")
    @Operation(summary = "Get most liked posts", description = "Returns top N posts ranked by like count")
    public ResponseEntity<ApiResponse<List<TopPostDTO>>> getMostLikedPosts(
            @Parameter(description = "Number of top posts to return") @RequestParam(defaultValue = "10") int limit) {
        List<TopPostDTO> posts = analyticsService.getMostLikedPosts(limit);
        return ResponseEntity.ok(ApiResponse.success("Most liked posts retrieved successfully", posts));
    }

    @GetMapping("/posts/top-popular")
    @Operation(summary = "Get most popular posts", description = "Returns top N posts by combined popularity score (likes)")
    public ResponseEntity<ApiResponse<List<TopPostDTO>>> getMostPopularPosts(
            @Parameter(description = "Number of top posts to return") @RequestParam(defaultValue = "10") int limit) {
        List<TopPostDTO> posts = analyticsService.getMostLikedPosts(limit);
        return ResponseEntity.ok(ApiResponse.success("Most popular posts retrieved successfully", posts));
    }

    @GetMapping("/authors/top")
    @Operation(summary = "Get top authors", description = "Returns top N authors ranked by total views and likes across all their posts")
    public ResponseEntity<ApiResponse<List<TopAuthorDTO>>> getTopAuthors(
            @Parameter(description = "Number of top authors to return") @RequestParam(defaultValue = "10") int limit) {
        List<TopAuthorDTO> authors = analyticsService.getTopAuthors(limit);
        return ResponseEntity.ok(ApiResponse.success("Top authors retrieved successfully", authors));
    }

    @PostMapping("/export")
    @Operation(summary = "Export analytics report", description = "Exports analytics report in PDF or Excel format")
    public ResponseEntity<ByteArrayResource> exportReport(
            @Valid @RequestBody ReportRequestDTO request) {

        byte[] reportData = switch (request.exportFormat()) {
            case PDF -> analyticsExportService.generatePdfReport(request);
            case EXCEL -> analyticsExportService.generateExcelReport(request);
        };

        String filename = generateFileName(request);
        MediaType mediaType = request.exportFormat() == ReportRequestDTO.ExportFormat.PDF
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        ByteArrayResource resource = new ByteArrayResource(reportData);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    private String generateFileName(ReportRequestDTO request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String period = switch (request.reportType()) {
            case MONTH -> request.year() + "_" + String.format("%02d", request.month());
            case QUARTER -> request.year() + "_Q" + request.quarter();
            case YEAR -> String.valueOf(request.year());
        };
        return "analytics_report_" + period + "_" + timestamp + "." + 
               (request.exportFormat() == ReportRequestDTO.ExportFormat.PDF ? "pdf" : "xlsx");
    }
}