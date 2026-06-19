package com.Nguyen.blogplatform.domain.analytics.dto;



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