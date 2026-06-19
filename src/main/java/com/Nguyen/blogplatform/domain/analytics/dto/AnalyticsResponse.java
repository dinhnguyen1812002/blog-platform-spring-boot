package com.Nguyen.blogplatform.domain.analytics.dto;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsResponse {
    private long totalUsers;
    private long totalPosts;
    private long totalTags;
    private long totalCategories;
    private long totalSubscribers;

    private double userGrowth;
    private double postGrowth;
    private double subscriberGrowth;
}

