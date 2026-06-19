package com.Nguyen.blogplatform.domain.auth.application;



import com.Nguyen.blogplatform.domain.auth.dto.OAuthProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthProviderService {

    private final RestTemplate restTemplate;

    public OAuthProfileResponse verifyAndFetchProfile(String provider, String accessToken) {
        return switch (provider.toLowerCase()) {
            case "google" -> fetchGoogleProfile(accessToken);
            case "github" -> fetchGithubProfile(accessToken);
            case "discord" -> fetchDiscordProfile(accessToken);
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }

    private OAuthProfileResponse fetchGoogleProfile(String accessToken) {
        Map<String, Object> payload = getWithBearer("https://www.googleapis.com/oauth2/v3/userinfo", accessToken);
        return new OAuthProfileResponse(
                "google",
                String.valueOf(payload.get("sub")),
                (String) payload.get("email"),
                (String) payload.get("name"),
                (String) payload.get("picture")
        );
    }

    private OAuthProfileResponse fetchGithubProfile(String accessToken) {
        Map<String, Object> payload = getWithBearer("https://api.github.com/user", accessToken);
        String email = (String) payload.get("email");
        if (email == null) {
            email = payload.get("login") + "@github.local";
        }
        return new OAuthProfileResponse(
                "github",
                String.valueOf(payload.get("id")),
                email,
                (String) payload.getOrDefault("name", payload.get("login")),
                (String) payload.get("avatar_url")
        );
    }

    private OAuthProfileResponse fetchDiscordProfile(String accessToken) {
        Map<String, Object> payload = getWithBearer("https://discord.com/api/users/@me", accessToken);
        String avatarHash = (String) payload.get("avatar");
        String userId = String.valueOf(payload.get("id"));
        String avatar = avatarHash == null
                ? null
                : "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarHash + ".png";
        return new OAuthProfileResponse(
                "discord",
                userId,
                (String) payload.get("email"),
                (String) payload.get("username"),
                avatar
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getWithBearer(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new IllegalArgumentException("Empty response from OAuth provider");
        }
        return body;
    }
}
