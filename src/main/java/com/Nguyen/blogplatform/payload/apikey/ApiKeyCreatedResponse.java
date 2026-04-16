package com.Nguyen.blogplatform.payload.apikey;

import java.time.LocalDateTime;

public record ApiKeyCreatedResponse(
    String id,
    String apiKey,
    String apiSecret,
    String name,
    LocalDateTime createdAt
) {}
