package com.Nguyen.blogplatform.payload.response.analytics;

import lombok.Builder;

@Builder
public record MonthlyGrowthDTO(
    int year,
    int month,
    long newUsers,
    long newPosts,
    long totalViews,
    long totalLikes,
    double growthRate
) {}