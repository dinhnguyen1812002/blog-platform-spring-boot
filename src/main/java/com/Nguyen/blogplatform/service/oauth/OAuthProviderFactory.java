package com.Nguyen.blogplatform.service.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OAuthProviderFactory {

    private final List<OAuthService> oauthServices;

    @Autowired
    public OAuthProviderFactory(List<OAuthService> oauthServices) {
        this.oauthServices = oauthServices;
    }

    public OAuthService getService(String provider) {
        return oauthServices.stream()
            .filter(service -> service.supports(provider))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported OAuth provider: " + provider));
    }

    public OAuthService getServiceByEnum(com.Nguyen.blogplatform.Enum.EOAuthProvider provider) {
        return oauthServices.stream()
            .filter(service -> service.getProvider() == provider)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported OAuth provider: " + provider));
    }
}
