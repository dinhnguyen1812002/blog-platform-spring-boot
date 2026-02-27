package com.Nguyen.blogplatform.payload.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthTokenRequest(
    @NotBlank String provider,
    @NotBlank String accessToken
) {}
