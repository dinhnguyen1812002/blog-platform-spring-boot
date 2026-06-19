package com.Nguyen.blogplatform.domain.auth.dto;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {

    private String refreshToken;
}