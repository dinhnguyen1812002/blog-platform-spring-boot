package com.Nguyen.blogplatform.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {

    private String refreshToken;
}