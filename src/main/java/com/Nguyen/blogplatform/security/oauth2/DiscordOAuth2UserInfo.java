package com.Nguyen.blogplatform.security.oauth2;

import java.util.Map;

public class DiscordOAuth2UserInfo extends OAuth2UserInfo {
    public DiscordOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() { return String.valueOf(attributes.get("id")); }

    @Override
    public String getName() {
        String globalName = (String) attributes.get("global_name");
        return globalName != null ? globalName : (String) attributes.get("username");
    }

    @Override
    public String getEmail() { return (String) attributes.get("email"); }

    @Override
    public String getImageUrl() {
        String id = getId();
        String avatarHash = (String) attributes.get("avatar");
        return (avatarHash != null) ? String.format("https://cdn.discordapp.com/avatars/%s/%s.png", id, avatarHash) : null;
    }
}
