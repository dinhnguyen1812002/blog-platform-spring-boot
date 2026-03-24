package com.Nguyen.blogplatform.payload.response.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAuthorizationResponse {

    private String authorizationUrl;
    private String state;
    private String codeChallenge;
    private Long expiresIn;
}
