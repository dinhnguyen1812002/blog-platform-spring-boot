package com.Nguyen.blogplatform.security.oauth2;

import com.Nguyen.blogplatform.Enum.EOAuthProvider;
import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(EOAuthProvider.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(EOAuthProvider.GITHUB.toString())) {
            return new GithubOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(EOAuthProvider.DISCORD.toString())) {
            return new DiscordOAuth2UserInfo(attributes);
        } else {
            throw new RuntimeException("Provider " + registrationId + " is not supported yet.");
        }
    }
}
