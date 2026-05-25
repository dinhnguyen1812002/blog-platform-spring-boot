package com.Nguyen.blogplatform.service.analytics.impl;

import com.Nguyen.blogplatform.payload.response.analytics.*;
import com.Nguyen.blogplatform.repository.*;
import com.Nguyen.blogplatform.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final OAuthAuditLogRepository oauthAuditLogRepository;

    private static final int ACTIVE_USER_DAYS = 30;

    @Override
    @Cacheable(value = "analytics", key = "'dashboard-summary'")
    public DashboardSummaryDTO getDashboardSummary() {
        LocalDateTime now = LocalDateTime.now();

        Long viewCount = postRepository.sumAllViewCounts();
        long totalViews = viewCount != null ? viewCount : 0L;

        LocalDateTime activeSince = now.minusDays(ACTIVE_USER_DAYS);
        long activeUsers = userRepository.countActiveUsersByLogin(activeSince);

        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        long newPosts = postRepository.countByCreatedAtBetween(startOfMonth, now);

        Long totalLikes = postRepository.sumAllLikes();

        return DashboardSummaryDTO.builder()
                .totalViews(totalViews)
                .activeUsers(activeUsers)
                .newPosts(newPosts)
                .totalLikes(totalLikes != null ? totalLikes : 0L)
                .build();
    }

    @Override
    @Cacheable(value = "analytics", key = "'new-users-per-month-' + #year")
    public List<MonthlyStatDTO> getNewUsersPerMonth(int year) {
        return userRepository.countNewUsersPerMonth(year);
    }

    @Override
    @Cacheable(value = "analytics", key = "'post-growth-per-month-' + #year")
    public List<MonthlyStatDTO> getPostGrowthPerMonth(int year) {
        return postRepository.countPostsPerMonth(year);
    }

    @Override
    @Cacheable(value = "analytics", key = "'monthly-growth-' + #year")
    public List<MonthlyGrowthDTO> getMonthlyGrowth(int year) {
        List<Object[]> monthlyData = postRepository.getMonthlyStatsWithViewsAndPosts(year);
        List<MonthlyStatDTO> monthlyUsers = userRepository.countNewUsersPerMonth(year);

        List<MonthlyGrowthDTO> result = new ArrayList<>();

        for (Object[] row : monthlyData) {
            int rowYear = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            Long views = (Long) row[2];
            Long posts = (Long) row[3];

            long newUsers = monthlyUsers.stream()
                    .filter(m -> m.month() == month)
                    .mapToLong(MonthlyStatDTO::count)
                    .findFirst()
                    .orElse(0L);

            long likes = 0;
            if (posts > 0) {
                likes = Math.round((posts * 0.3));
            }

            double growthRate = calculateGrowthRate(year, month, posts, newUsers, views);

            result.add(MonthlyGrowthDTO.builder()
                    .year(rowYear)
                    .month(month)
                    .newUsers(newUsers)
                    .newPosts(posts)
                    .totalViews(views != null ? views : 0L)
                    .totalLikes(likes)
                    .growthRate(growthRate)
                    .build());
        }

        return result;
    }

    private double calculateGrowthRate(int year, int month, long currentPosts, long currentUsers, long currentViews) {
        final int previousMonth = month == 1 ? 12 : month - 1;
        final int previousYear = month == 1 ? year - 1 : year;

        List<Object[]> previousData = postRepository.getMonthlyStatsWithViewsAndPosts(previousYear);
        List<MonthlyStatDTO> previousUsers = userRepository.countNewUsersPerMonth(previousYear);

        long prevPosts = previousData.stream()
                .filter(row -> ((Number) row[1]).intValue() == previousMonth)
                .mapToLong(row -> ((Long) row[3]))
                .findFirst()
                .orElse(0L);

        long prevUsers = previousUsers.stream()
                .filter(m -> m.month() == previousMonth)
                .mapToLong(MonthlyStatDTO::count)
                .findFirst()
                .orElse(0L);

        long totalPrev = prevPosts + prevUsers;
        long totalCurrent = currentPosts + currentUsers;

        if (totalPrev == 0) {
            return totalCurrent > 0 ? 100.0 : 0.0;
        }

        return ((double) (totalCurrent - totalPrev) / totalPrev) * 100;
    }

    @Override
    @Cacheable(value = "analytics", key = "'most-viewed-posts-' + #limit")
    public List<TopPostDTO> getMostViewedPosts(int limit) {
        return postRepository.findTopPostsByViews(PageRequest.of(0, limit));
    }

    @Override
    @Cacheable(value = "analytics", key = "'most-liked-posts-' + #limit")
    public List<TopPostDTO> getMostLikedPosts(int limit) {
        return postRepository.findTopPostsByLikes(PageRequest.of(0, limit));
    }

    @Override
    @Cacheable(value = "analytics", key = "'top-authors-' + #limit")
    public List<TopAuthorDTO> getTopAuthors(int limit) {
        return userRepository.findTopAuthorsByEngagement(PageRequest.of(0, limit));
    }
}