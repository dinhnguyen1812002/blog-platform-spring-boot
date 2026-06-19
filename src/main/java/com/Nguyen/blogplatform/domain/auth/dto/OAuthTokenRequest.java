package com.Nguyen.blogplatform.domain.auth.dto;



import jakarta.validation.constraints.NotBlank;

public record OAuthTokenRequest(
        @NotBlank String provider,
        @NotBlank String accessToken
) {}
