package com.Nguyen.blogplatform.domain.apikey.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApiKeyCreateRequest(
    @NotBlank(message = "API key name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    String name
) {}
