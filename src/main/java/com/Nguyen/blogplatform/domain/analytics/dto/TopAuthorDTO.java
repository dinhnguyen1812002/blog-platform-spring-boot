package com.Nguyen.blogplatform.domain.analytics.dto;



public record TopAuthorDTO(
    String authorId,
    String authorName,
    long totalViews,
    long totalLikes,
    long postCount
) {}