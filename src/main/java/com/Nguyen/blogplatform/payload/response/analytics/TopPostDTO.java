package com.Nguyen.blogplatform.payload.response.analytics;

public record TopPostDTO(
    String postId,
    String title,
    String authorName,
    long viewCount,
    long likeCount
) {}