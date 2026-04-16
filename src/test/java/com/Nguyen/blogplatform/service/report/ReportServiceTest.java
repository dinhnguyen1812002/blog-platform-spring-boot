package com.Nguyen.blogplatform.service.report;

import com.Nguyen.blogplatform.Enum.ReportCategory;
import com.Nguyen.blogplatform.Enum.ReportStatus;
import com.Nguyen.blogplatform.exception.ConflictException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.exception.RateLimitException;
import com.Nguyen.blogplatform.model.ArticleReport;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.ReportRequest;
import com.Nguyen.blogplatform.payload.response.ReportResponse;
import com.Nguyen.blogplatform.repository.ArticleReportRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for ReportService.
 * Tests report submission, duplicate prevention, rate limiting, and validation.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ArticleReportRepository reportRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReportService reportService;

    private User testUser;
    private User testAuthor;
    private Post testPost;
    private ReportRequest reportRequest;

    @BeforeEach
    void setUp() {
        // Setup test user (reporter)
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Setup test author
        testAuthor = new User();
        testAuthor.setId("author-456");
        testAuthor.setUsername("testauthor");
        testAuthor.setEmail("author@example.com");

        // Setup test post
        testPost = new Post();
        testPost.setId("post-789");
        testPost.setTitle("Test Post");
        testPost.setSlug("test-post");
        testPost.setAuthor(testAuthor);

        // Setup report request
        reportRequest = new ReportRequest(
                ReportCategory.SPAM_MISLEADING,
                "This post contains spam content"
        );
    }

    @Test
    @DisplayName("Submit report successfully")
    void submitReport_Success() {
        // Arrange
        when(postRepository.findById("post-789")).thenReturn(Optional.of(testPost));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(reportRepository.existsByReporterAndPost(testUser, testPost)).thenReturn(false);
        when(reportRepository.countByReporterAndCreatedAtAfter(any(), any())).thenReturn(0L);
        when(reportRepository.save(any(ArticleReport.class))).thenAnswer(invocation -> {
            ArticleReport report = invocation.getArgument(0);
            report.setId("report-001");
            report.setCreatedAt(LocalDateTime.now());
            report.setUpdatedAt(LocalDateTime.now());
            return report;
        });

        // Act
        ReportResponse response = reportService.submitReport("post-789", reportRequest, "user-123");

        // Assert
        assertNotNull(response);
        assertEquals("post-789", response.postId());
        assertEquals("Test Post", response.postTitle());
        assertEquals(ReportCategory.SPAM_MISLEADING, response.category());
        assertEquals(ReportStatus.PENDING, response.status());

        // Verify
        verify(postRepository).findById("post-789");
        verify(userRepository).findById("user-123");
        verify(reportRepository).existsByReporterAndPost(testUser, testPost);
        verify(reportRepository).save(any(ArticleReport.class));
        verify(notificationService).createUserNotification(
                eq("author-456"),
                eq("REPORT_SUBMITTED"),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Submit report fails when post not found")
    void submitReport_PostNotFound() {
        // Arrange
        when(postRepository.findById("invalid-post")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> 
                reportService.submitReport("invalid-post", reportRequest, "user-123"));
        
        verify(postRepository).findById("invalid-post");
        verifyNoMoreInteractions(userRepository, reportRepository);
    }

    @Test
    @DisplayName("Submit report fails when user not found")
    void submitReport_UserNotFound() {
        // Arrange
        when(postRepository.findById("post-789")).thenReturn(Optional.of(testPost));
        when(userRepository.findById("invalid-user")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> 
                reportService.submitReport("post-789", reportRequest, "invalid-user"));
        
        verify(postRepository).findById("post-789");
        verify(userRepository).findById("invalid-user");
        verifyNoMoreInteractions(reportRepository);
    }

    @Test
    @DisplayName("Submit report fails with duplicate report")
    void submitReport_DuplicateReport() {
        // Arrange
        when(postRepository.findById("post-789")).thenReturn(Optional.of(testPost));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(reportRepository.existsByReporterAndPost(testUser, testPost)).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> 
                reportService.submitReport("post-789", reportRequest, "user-123"));
        
        assertEquals("You have already reported this post", 
                assertThrows(ConflictException.class, () -> 
                        reportService.submitReport("post-789", reportRequest, "user-123")).getMessage());
        
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Submit report fails when rate limit exceeded")
    void submitReport_RateLimitExceeded() {
        // Arrange
        when(postRepository.findById("post-789")).thenReturn(Optional.of(testPost));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(reportRepository.existsByReporterAndPost(testUser, testPost)).thenReturn(false);
        when(reportRepository.countByReporterAndCreatedAtAfter(any(), any())).thenReturn(5L);

        // Act & Assert
        RateLimitException exception = assertThrows(RateLimitException.class, () -> 
                reportService.submitReport("post-789", reportRequest, "user-123"));
        
        assertTrue(exception.getMessage().contains("Too many reports submitted"));
        assertTrue(exception.getMessage().contains("5 reports per hour"));
        
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Submit report with OTHER category requires description")
    void submitReport_OtherCategoryRequiresDescription() {
        // Arrange
        ReportRequest otherRequest = new ReportRequest(ReportCategory.OTHER, null);
        lenient().when(postRepository.findById("post-789")).thenReturn(Optional.of(testPost));
        lenient().when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        lenient().when(reportRepository.existsByReporterAndPost(testUser, testPost)).thenReturn(false);
        lenient().when(reportRepository.countByReporterAndCreatedAtAfter(any(), any())).thenReturn(0L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                reportService.submitReport("post-789", otherRequest, "user-123"));
        
        assertTrue(exception.getMessage().contains("Description is required"));
    }

    @Test
    @DisplayName("Get report by ID successfully")
    void getReportById_Success() {
        // Arrange
        ArticleReport report = ArticleReport.builder()
                .id("report-001")
                .post(testPost)
                .reporter(testUser)
                .category(ReportCategory.SPAM_MISLEADING)
                .description("Spam content")
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(reportRepository.findById("report-001")).thenReturn(Optional.of(report));

        // Act
        ReportResponse response = reportService.getReportById("report-001", "user-123");

        // Assert
        assertNotNull(response);
        assertEquals("report-001", response.id());
        assertEquals("post-789", response.postId());
        assertEquals(ReportStatus.PENDING, response.status());
    }

    @Test
    @DisplayName("Get report fails when report not found")
    void getReportById_ReportNotFound() {
        // Arrange
        when(reportRepository.findById("invalid-report")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> 
                reportService.getReportById("invalid-report", "user-123"));
    }

    @Test
    @DisplayName("Get report fails when user is not the reporter")
    void getReportById_Forbidden() {
        // Arrange
        ArticleReport report = ArticleReport.builder()
                .id("report-001")
                .post(testPost)
                .reporter(testUser)
                .category(ReportCategory.SPAM_MISLEADING)
                .status(ReportStatus.PENDING)
                .build();

        when(reportRepository.findById("report-001")).thenReturn(Optional.of(report));

        // Act & Assert
        String differentUserId = "different-user-999";
        assertThrows(com.Nguyen.blogplatform.exception.ForbiddenException.class, () -> 
                reportService.getReportById("report-001", differentUserId));
    }

    @Test
    @DisplayName("Submit report with all categories successfully")
    void submitReport_AllCategories() {
        // Arrange
        ReportCategory[] categories = ReportCategory.values();
        
        for (ReportCategory category : categories) {
            String description = category == ReportCategory.OTHER ? "Custom reason" : "Test description";
            ReportRequest request = new ReportRequest(category, description);

            when(postRepository.findById("post-789")).thenReturn(Optional.of(testPost));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
            when(reportRepository.existsByReporterAndPost(testUser, testPost)).thenReturn(false);
            when(reportRepository.countByReporterAndCreatedAtAfter(any(), any())).thenReturn(0L);
            when(reportRepository.save(any(ArticleReport.class))).thenAnswer(invocation -> {
                ArticleReport report = invocation.getArgument(0);
                report.setId("report-" + category.name());
                report.setCreatedAt(LocalDateTime.now());
                return report;
            });

            // Act
            ReportResponse response = reportService.submitReport("post-789", request, "user-123");

            // Assert
            assertNotNull(response);
            assertEquals(category, response.category());

            // Reset mocks for next iteration
            reset(reportRepository, postRepository, userRepository, notificationService);
        }
    }
}
