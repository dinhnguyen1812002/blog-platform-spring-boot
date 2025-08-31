package com.Nguyen.blogplatform.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
    private String userId;
    private String username;
    private long totalPosts;
    private List<PostResponse> mostViewedPosts;
    private List<PostResponse> mostLikedPosts;
    private Map<String, Object> trafficStatistics;
    private Map<String, Long> monthlyReadStatistics;
    private Map<Integer, Long> yearlyReadStatistics;
}