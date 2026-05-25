package com.Nguyen.blogplatform.payload.response.analytics;

import lombok.Builder;

@Builder
public record DashboardSummaryDTO(
    long totalViews,
    long activeUsers,
    long newPosts,
    long totalLikes
) {}