package com.Nguyen.blogplatform.domain.auth.dto;



public record OAuthProfileResponse(
        String provider,
        String providerId,
        String email,
        String name,
        String avatar
) {}
