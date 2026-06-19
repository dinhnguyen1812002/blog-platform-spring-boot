package com.Nguyen.blogplatform.domain.apikey.dto;



import java.time.LocalDateTime;

public record ExternalPostResponse(
    String id,
    String title,
    String excerpt,
    String slug,
    String content,
    String thumbnail,
    LocalDateTime publishedAt,
    String authorUsername
) {}
