package com.Nguyen.blogplatform.dto.apikey;

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
