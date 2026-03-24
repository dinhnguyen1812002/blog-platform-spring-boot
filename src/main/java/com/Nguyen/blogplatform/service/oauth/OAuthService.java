package com.Nguyen.blogplatform.service.oauth;

import com.Nguyen.blogplatform.Enum.EOAuthProvider;
import com.Nguyen.blogplatform.payload.request.oauth.OAuthCallbackRequest;
import com.Nguyen.blogplatform.payload.response.oauth.OAuthAuthorizationResponse;
import com.Nguyen.blogplatform.payload.response.oauth.OAuthTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface OAuthService {

    EOAuthProvider getProvider();

    OAuthAuthorizationResponse initiateAuthorization(String redirectUri, String state, HttpServletRequest request);

    OAuthTokenResponse handleCallback(OAuthCallbackRequest callbackRequest, HttpServletRequest request, HttpServletResponse response);

    void unlinkAccount(String userId, String accountId);

    boolean supports(String provider);
}
