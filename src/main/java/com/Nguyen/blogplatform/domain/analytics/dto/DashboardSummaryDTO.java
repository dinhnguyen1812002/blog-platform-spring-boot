package com.Nguyen.blogplatform.domain.analytics.dto;



import lombok.Builder;

@Builder
public record DashboardSummaryDTO(
    long totalViews,
    long activeUsers,
    long newPosts,
    long totalLikes
) {}