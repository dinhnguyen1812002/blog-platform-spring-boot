package com.Nguyen.blogplatform.controller.Statistics;

import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.UserStatisticsResponse;
import com.Nguyen.blogplatform.service.UserDetailsImpl;
import com.Nguyen.blogplatform.service.UserStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "User Statistics", description = "User statistics management")
public class UserStatisticsController {

    private final UserStatisticsService userStatisticsService;

    @GetMapping("/user/{userId}/most-viewed")
    @Operation(summary = "Get most viewed posts for a user", description = "Get the most viewed posts for a user")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<List<PostResponse>> getMostViewedPosts(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Maximum number of posts to return") @RequestParam(defaultValue = "5") int limit) {
        List<PostResponse> posts = userStatisticsService.getMostViewedPosts(userId, limit);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/user/{userId}/most-liked")
    @Operation(summary = "Get most liked posts for a user", description = "Get the most liked posts for a user")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<List<PostResponse>> getMostLikedPosts(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Maximum number of posts to return") @RequestParam(defaultValue = "5") int limit) {
        List<PostResponse> posts = userStatisticsService.getMostLikedPosts(userId, limit);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/user/{userId}/post-count")
    @Operation(summary = "Get total post count for a user", description = "Get the total number of posts for a user")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<Long> getTotalPostCount(
            @Parameter(description = "User ID") @PathVariable String userId) {
        long count = userStatisticsService.getTotalPostCount(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/user/{userId}/traffic")
    @Operation(summary = "Get traffic statistics for a user", description = "Get traffic statistics for a user")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<Map<String, Object>> getTrafficStatistics(
            @Parameter(description = "User ID") @PathVariable String userId) {
        Map<String, Object> statistics = userStatisticsService.getTrafficStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/user/{userId}/monthly-reads")
    @Operation(summary = "Get monthly read statistics for a user", description = "Get monthly read statistics for a user for a specific year")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<Map<String, Long>> getMonthlyReadStatistics(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Year") @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        Map<String, Long> statistics = userStatisticsService.getMonthlyReadStatistics(userId, year);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/user/{userId}/yearly-reads")
    @Operation(summary = "Get yearly read statistics for a user", description = "Get yearly read statistics for a user for a specific range of years")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<Map<Integer, Long>> getYearlyReadStatistics(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Start year") @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear() - 5}") int startYear,
            @Parameter(description = "End year") @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int endYear) {
        Map<Integer, Long> statistics = userStatisticsService.getYearlyReadStatistics(userId, startYear, endYear);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all statistics for a user", description = "Get comprehensive statistics for a user")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Maximum number of posts to return for most viewed and most liked") @RequestParam(defaultValue = "5") int limit,
            @Parameter(description = "Year for monthly statistics") @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            @Parameter(description = "Start year for yearly statistics") @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear() - 5}") int startYear,
            @Parameter(description = "End year for yearly statistics") @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int endYear) {
        UserStatisticsResponse statistics = userStatisticsService.getUserStatistics(userId, limit, year, startYear, endYear);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/me")
    @Operation(summary = "Get all statistics for the current user", description = "Get comprehensive statistics for the current user")
    public ResponseEntity<UserStatisticsResponse> getCurrentUserStatistics(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Maximum number of posts to return for most viewed and most liked") @RequestParam(defaultValue = "5") int limit,
            @Parameter(description = "Year for monthly statistics") @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            @Parameter(description = "Start year for yearly statistics") @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear() - 5}") int startYear,
            @Parameter(description = "End year for yearly statistics") @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int endYear) {
        UserStatisticsResponse statistics = userStatisticsService.getUserStatistics(userDetails.getId(), limit, year, startYear, endYear);
        return ResponseEntity.ok(statistics);
    }
}