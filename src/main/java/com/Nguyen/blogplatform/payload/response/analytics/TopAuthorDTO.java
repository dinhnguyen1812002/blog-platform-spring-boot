package com.Nguyen.blogplatform.payload.response.analytics;

public record TopAuthorDTO(
    String authorId,
    String authorName,
    long totalViews,
    long totalLikes,
    long postCount
) {}