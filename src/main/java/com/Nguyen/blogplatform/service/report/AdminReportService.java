package com.Nguyen.blogplatform.service.report;

import com.Nguyen.blogplatform.Enum.ReportCategory;
import com.Nguyen.blogplatform.Enum.ReportStatus;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.ArticleReport;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.response.AdminReportResponse;
import com.Nguyen.blogplatform.repository.ArticleReportRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin service for managing article reports.
 * Provides moderation capabilities and statistics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportService {

    private final ArticleReportRepository reportRepository;
    private final UserRepository userRepository;

    /**
     * Get all reports with filtering and pagination.
     *
     * @param status Filter by status (optional)
     * @param category Filter by category (optional)
     * @param pageable Pagination information
     * @return Page of admin report responses
     */
    @Transactional(readOnly = true)
    public Page<AdminReportResponse> getAllReports(
            ReportStatus status, 
            ReportCategory category, 
            Pageable pageable) {
        
        Page<ArticleReport> reports;
        
        if (status != null && category != null) {
            reports = reportRepository.findByStatusAndCategory(status, category, pageable);
        } else if (status != null) {
            reports = reportRepository.findByStatus(status, pageable);
        } else if (category != null) {
            reports = reportRepository.findByCategory(category, pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }
        
        return reports.map(this::mapToAdminReportResponse);
    }

    /**
     * Get pending reports for moderation queue.
     *
     * @param pageable Pagination information
     * @return Page of pending reports
     */
    @Transactional(readOnly = true)
    public Page<AdminReportResponse> getPendingReports(Pageable pageable) {
        return reportRepository.findPendingReports(pageable)
                .map(this::mapToAdminReportResponse);
    }

    /**
     * Get a specific report by ID with full details.
     *
     * @param reportId Report ID
     * @return Admin report response with full details
     */
    @Transactional(readOnly = true)
    public AdminReportResponse getReportById(String reportId) {
        ArticleReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found with ID: " + reportId));
        
        return mapToAdminReportResponse(report);
    }

    /**
     * Update the status of a report.
     *
     * @param reportId Report ID
     * @param newStatus New status to set
     * @param adminNotes Admin/moderator notes
     * @param reviewerId ID of the admin/moderator reviewing the report
     * @return Updated report
     */
    @Transactional
    public AdminReportResponse updateReportStatus(
            String reportId, 
            ReportStatus newStatus, 
            String adminNotes, 
            String reviewerId) {
        
        ArticleReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found with ID: " + reportId));
        
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found with ID: " + reviewerId));
        
        report.setStatus(newStatus);
        report.setAdminNotes(adminNotes);
        report.setReviewedBy(reviewer);
        report.setReviewedAt(LocalDateTime.now());
        
        ArticleReport updatedReport = reportRepository.save(report);
        log.info("Report {} status updated to {} by {}", reportId, newStatus, reviewer.getUsername());
        
        return mapToAdminReportResponse(updatedReport);
    }

    /**
     * Delete a report.
     *
     * @param reportId Report ID
     */
    @Transactional
    public void deleteReport(String reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new NotFoundException("Report not found with ID: " + reportId);
        }
        
        reportRepository.deleteById(reportId);
        log.info("Report {} deleted", reportId);
    }

    /**
     * Get statistics for the admin dashboard.
     *
     * @return Map of statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReportStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Overall counts
        stats.put("totalReports", reportRepository.count());
        stats.put("pendingReports", reportRepository.countByStatus(ReportStatus.PENDING));
        stats.put("resolvedReports", reportRepository.countByStatus(ReportStatus.RESOLVED));
        stats.put("dismissedReports", reportRepository.countByStatus(ReportStatus.DISMISSED));
        
        // Today's reports
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        stats.put("reportsToday", reportRepository.countByCreatedAtAfter(startOfDay));
        
        // Reports by category
        Map<String, Long> categoryStats = new HashMap<>();
        for (ReportCategory category : ReportCategory.values()) {
            categoryStats.put(category.name(), reportRepository.countByCategory(category));
        }
        stats.put("byCategory", categoryStats);
        
        return stats;
    }

    /**
     * Map ArticleReport entity to AdminReportResponse DTO.
     *
     * @param report The report entity
     * @return The admin response DTO
     */
    private AdminReportResponse mapToAdminReportResponse(ArticleReport report) {
        return new AdminReportResponse(
                report.getId(),
                report.getPost().getId(),
                report.getPost().getTitle(),
                report.getPost().getSlug(),
                report.getPost().getAuthor().getUsername(),
                report.getReporter().getId(),
                report.getReporter().getUsername(),
                report.getReporter().getEmail(),
                report.getCategory(),
                report.getDescription(),
                report.getStatus(),
                report.getAdminNotes(),
                report.getReviewedBy() != null ? report.getReviewedBy().getUsername() : null,
                report.getReviewedAt(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
