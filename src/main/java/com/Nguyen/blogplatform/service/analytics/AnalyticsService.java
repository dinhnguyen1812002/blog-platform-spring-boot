package com.Nguyen.blogplatform.service.analytics;

import com.Nguyen.blogplatform.payload.response.analytics.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnalyticsService {

    DashboardSummaryDTO getDashboardSummary();

    List<MonthlyStatDTO> getNewUsersPerMonth(int year);

    List<MonthlyStatDTO> getPostGrowthPerMonth(int year);

    List<MonthlyGrowthDTO> getMonthlyGrowth(int year);

    List<TopPostDTO> getMostViewedPosts(int limit);

    List<TopPostDTO> getMostLikedPosts(int limit);

    List<TopAuthorDTO> getTopAuthors(int limit);
}