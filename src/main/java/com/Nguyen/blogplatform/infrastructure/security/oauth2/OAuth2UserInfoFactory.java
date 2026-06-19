package com.Nguyen.blogplatform.infrastructure.security.oauth2;




import com.Nguyen.blogplatform.shared.enums.EOAuthProvider;
import java.util.Map;

public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(EOAuthProvider.GOOGLE.name())) {
            return new GoogleOAuth2UserInfo(attributes);
        }
        if (registrationId.equalsIgnoreCase(EOAuthProvider.GITHUB.name())) {
            return new GithubOAuth2UserInfo(attributes);
        }
        if (registrationId.equalsIgnoreCase(EOAuthProvider.DISCORD.name())) {
            return new DiscordOAuth2UserInfo(attributes);
        }
        throw new IllegalArgumentException("Provider " + registrationId + " is not supported.");
    }
}
