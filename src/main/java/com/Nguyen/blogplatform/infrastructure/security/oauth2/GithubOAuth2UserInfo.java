package com.Nguyen.blogplatform.infrastructure.security.oauth2;



import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        String name = (String) attributes.get("name");
        return name != null ? name : (String) attributes.get("login");
    }

    @Override
    public String getEmail() {
        String email = (String) attributes.get("email");
        if (email == null) {
            return attributes.get("login") + "@github.local";
        }
        return email;
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}
