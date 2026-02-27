package com.Nguyen.blogplatform.payload.response;

public record OAuthProfileResponse(
    String provider,
    String providerId,
    String email,
    String name,
    String avatar
) {}
