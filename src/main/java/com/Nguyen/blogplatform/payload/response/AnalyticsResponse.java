package com.Nguyen.blogplatform.payload.response;

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

