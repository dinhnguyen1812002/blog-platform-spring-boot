package com.Nguyen.blogplatform.domain.apikey.dto;



import java.time.LocalDateTime;

public record ApiKeyCreatedResponse(
    String id,
    String apiKey,
    String apiSecret,
    String name,
    LocalDateTime createdAt
) {}
