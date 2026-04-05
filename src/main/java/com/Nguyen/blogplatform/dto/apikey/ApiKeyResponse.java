package com.Nguyen.blogplatform.dto.apikey;

import java.time.LocalDateTime;

public record ApiKeyResponse(
    String id,
    String apiKey,
    String name,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime lastUsedAt
) {}
