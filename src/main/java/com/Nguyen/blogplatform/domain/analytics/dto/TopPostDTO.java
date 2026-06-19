package com.Nguyen.blogplatform.domain.analytics.dto;



public record TopPostDTO(
    String postId,
    String title,
    String authorName,
    long viewCount,
    long likeCount
) {}