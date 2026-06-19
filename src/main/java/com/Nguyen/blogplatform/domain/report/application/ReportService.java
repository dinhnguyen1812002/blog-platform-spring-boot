package com.Nguyen.blogplatform.domain.report.application;



import com.Nguyen.blogplatform.domain.notification.application.NotificationService;
import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.report.domain.model.ArticleReport;
import com.Nguyen.blogplatform.domain.report.dto.ReportRequest;
import com.Nguyen.blogplatform.domain.report.dto.ReportResponse;
import com.Nguyen.blogplatform.domain.report.infrastructure.repository.ArticleReportRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import com.Nguyen.blogplatform.shared.exception.ConflictException;
import com.Nguyen.blogplatform.shared.exception.ForbiddenException;
import com.Nguyen.blogplatform.shared.exception.NotFoundException;
import com.Nguyen.blogplatform.shared.exception.RateLimitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for handling article reports from users.
 * Includes duplicate prevention, rate limiting, and moderator notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ArticleReportRepository reportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Rate limiting configuration
    private static final int MAX_REPORTS_PER_HOUR = 5;
    private static final String NOTIFICATION_TYPE_REPORT_SUBMITTED = "REPORT_SUBMITTED";

    /**
     * Submit a new article report.
     * Includes duplicate prevention and rate limiting.
     *
     * @param postId ID of the post being reported
     * @param request Report details
     * @param reporterId ID of the user submitting the report
     * @return The created report
     */
    @Transactional
    public ReportResponse submitReport(String postId, ReportRequest request, String reporterId) {
        // Validate post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with ID: " + postId));

        // Validate reporter exists
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + reporterId));

        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Description is required when category is OTHER");
        }

        // Check for duplicate report
        if (reportRepository.existsByReporterAndPost(reporter, post)) {
            throw new ConflictException("You have already reported this post");
        }

        // Check rate limit
        enforceRateLimit(reporter);

        // Create and save the report
        ArticleReport report = ArticleReport.builder()
                .post(post)
                .reporter(reporter)
                .category(request.category())
                .description(request.description())
                .build();

        ArticleReport savedReport = reportRepository.save(report);
        log.info("Report submitted: {} for post '{}' by user {}", 
                request.category(), post.getTitle(), reporter.getUsername());

        // Notify moderators/admins
        notifyModerators(savedReport, post);

        // Return response
        return mapToReportResponse(savedReport);
    }

    /**
     * Get a report by ID for the reporter.
     *
     * @param reportId ID of the report
     * @param userId ID of the user (to verify ownership)
     * @return The report details
     */
    @Transactional(readOnly = true)
    public ReportResponse getReportById(String reportId, String userId) {
        ArticleReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found with ID: " + reportId));

        // Verify the user is the reporter
        if (!report.getReporter().getId().equals(userId)) {
            throw new ForbiddenException("You can only view your own reports");
        }

        return mapToReportResponse(report);
    }

    /**
     * Enforce rate limiting: max MAX_REPORTS_PER_HOUR reports per user per hour.
     *
     * @param reporter The user submitting the report
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    private void enforceRateLimit(User reporter) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentReports = reportRepository.countByReporterAndCreatedAtAfter(reporter, oneHourAgo);

        if (recentReports >= MAX_REPORTS_PER_HOUR) {
            throw new RateLimitException(
                    "Too many reports submitted. Please wait before submitting more reports. " +
                    "Maximum " + MAX_REPORTS_PER_HOUR + " reports per hour.");
        }
    }

    /**
     * Notify moderators/admins about the new report.
     *
     * @param report The submitted report
     * @param post The reported post
     */
    private void notifyModerators(ArticleReport report, Post post) {
        try {
            // Notify the post author that their post was reported
            notificationService.createUserNotification(
                    post.getAuthor().getId(),
                    NOTIFICATION_TYPE_REPORT_SUBMITTED,
                    "Post Reported",
                    "Your post '" + post.getTitle() + "' has been reported for review."
            );

            log.info("Moderator notification sent for report: {}", report.getId());
        } catch (Exception e) {
            // Don't fail the report submission if notification fails
            log.error("Failed to send moderator notification for report: {}", report.getId(), e);
        }
    }

    /**
     * Map ArticleReport entity to ReportResponse DTO.
     *
     * @param report The report entity
     * @return The response DTO
     */
    private ReportResponse mapToReportResponse(ArticleReport report) {
        return new ReportResponse(
                report.getId(),
                report.getPost().getId(),
                report.getPost().getTitle(),
                report.getPost().getSlug(),
                report.getCategory(),
                report.getDescription(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
