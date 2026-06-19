package com.Nguyen.blogplatform.domain.analytics.application;



import com.Nguyen.blogplatform.domain.analytics.dto.DashboardSummaryDTO;
import com.Nguyen.blogplatform.domain.analytics.dto.MonthlyGrowthDTO;
import com.Nguyen.blogplatform.domain.analytics.dto.MonthlyStatDTO;
import com.Nguyen.blogplatform.domain.analytics.dto.TopAuthorDTO;
import com.Nguyen.blogplatform.domain.analytics.dto.TopPostDTO;
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