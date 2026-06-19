package com.Nguyen.blogplatform.domain.report.infrastructure.repository;



import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.report.domain.model.ArticleReport;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.shared.enums.ReportCategory;
import com.Nguyen.blogplatform.shared.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ArticleReport entity.
 * Provides methods for querying and managing article reports.
 */
@Repository
public interface ArticleReportRepository extends JpaRepository<ArticleReport, String> {

    /**
     * Check if a user has already reported a specific post.
     * Used for duplicate prevention.
     *
     * @param reporter The user who submitted the report
     * @param post The post being reported
     * @return true if a report exists for this user-post combination
     */
    boolean existsByReporterAndPost(User reporter, Post post);

    /**
     * Find an existing report by user and post.
     *
     * @param reporter The user who submitted the report
     * @param post The post being reported
     * @return Optional containing the report if found
     */
    Optional<ArticleReport> findByReporterAndPost(User reporter, Post post);

    /**
     * Count reports by a user within a time range.
     * Used for rate limiting.
     *
     * @param reporter The user
     * @param startTime Start of the time range
     * @return Number of reports submitted by the user in the time range
     */
    @Query("SELECT COUNT(ar) FROM ArticleReport ar WHERE ar.reporter = :reporter AND ar.createdAt >= :startTime")
    long countByReporterAndCreatedAtAfter(@Param("reporter") User reporter, @Param("startTime") LocalDateTime startTime);

    /**
     * Find all reports for a specific post.
     *
     * @param post The post
     * @param pageable Pagination information
     * @return Page of reports for the post
     */
    @Query("SELECT ar FROM ArticleReport ar WHERE ar.post = :post ORDER BY ar.createdAt DESC")
    Page<ArticleReport> findByPost(@Param("post") Post post, Pageable pageable);

    /**
     * Find reports by status.
     *
     * @param status Report status
     * @param pageable Pagination information
     * @return Page of reports with the specified status
     */
    Page<ArticleReport> findByStatus(ReportStatus status, Pageable pageable);

    /**
     * Find reports by category.
     *
     * @param category Report category
     * @param pageable Pagination information
     * @return Page of reports with the specified category
     */
    Page<ArticleReport> findByCategory(ReportCategory category, Pageable pageable);

    /**
     * Find reports by status and category.
     *
     * @param status Report status
     * @param category Report category
     * @param pageable Pagination information
     * @return Page of reports matching both criteria
     */
    Page<ArticleReport> findByStatusAndCategory(ReportStatus status, ReportCategory category, Pageable pageable);

    /**
     * Find reports created within a date range.
     *
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination information
     * @return Page of reports created in the date range
     */
    @Query("SELECT ar FROM ArticleReport ar WHERE ar.createdAt BETWEEN :startDate AND :endDate ORDER BY ar.createdAt DESC")
    Page<ArticleReport> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate, 
                                                Pageable pageable);

    /**
     * Get count of reports by status.
     *
     * @param status Report status
     * @return Number of reports with the specified status
     */
    long countByStatus(ReportStatus status);

    /**
     * Get count of reports by category.
     *
     * @param category Report category
     * @return Number of reports with the specified category
     */
    long countByCategory(ReportCategory category);

    /**
     * Get count of reports created today.
     *
     * @param startOfDay Start of today
     * @return Number of reports created today
     */
    @Query("SELECT COUNT(ar) FROM ArticleReport ar WHERE ar.createdAt >= :startOfDay")
    long countByCreatedAtAfter(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Find the most reported posts (posts with highest number of reports).
     *
     * @param minReports Minimum number of reports
     * @return List of posts with their report counts
     */
    @Query("SELECT ar.post.id, COUNT(ar) as reportCount FROM ArticleReport ar " +
           "WHERE ar.status = :status " +
           "GROUP BY ar.post.id " +
           "HAVING COUNT(ar) >= :minReports " +
           "ORDER BY reportCount DESC")
    List<Object[]> findMostReportedPosts(@Param("status") ReportStatus status, 
                                         @Param("minReports") long minReports);

    /**
     * Find all pending reports for moderation queue.
     *
     * @param pageable Pagination information
     * @return Page of pending reports
     */
    @Query("SELECT ar FROM ArticleReport ar WHERE ar.status = 'PENDING' ORDER BY ar.createdAt ASC")
    Page<ArticleReport> findPendingReports(Pageable pageable);
}
