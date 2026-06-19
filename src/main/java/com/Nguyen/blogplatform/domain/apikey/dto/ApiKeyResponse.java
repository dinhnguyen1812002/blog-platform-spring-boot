package com.Nguyen.blogplatform.domain.apikey.dto;



import java.time.LocalDateTime;

public record ApiKeyResponse(
    String id,
    String apiKey,
    String name,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime lastUsedAt
) {}
