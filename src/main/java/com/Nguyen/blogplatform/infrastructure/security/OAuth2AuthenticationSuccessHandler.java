package com.Nguyen.blogplatform.infrastructure.security;



import com.Nguyen.blogplatform.domain.auth.application.RefreshTokenService;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Value("${frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String userId = getAttribute(oauth2User, "resolved_user_id");
        String email = getAttribute(oauth2User, "resolved_email");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("OAuth2 user not found: " + userId));

        String jwtToken = jwtUtils.generateTokenFromUserId(user.getId(), email);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        ResponseCookie jwtCookie = jwtUtils.generateCookieFromToken(jwtToken);
        ResponseCookie refreshCookie = refreshTokenService.generateRefreshTokenCookie(refreshToken.getToken());

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        String targetUrl = frontendUrl + "/oauth2/redirect?token=" + jwtToken;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getAttribute(OAuth2User user, String key) {
        Object value = user.getAttributes().get(key);
        if (value == null) {
            throw new IllegalStateException("Missing OAuth2 attribute: " + key);
        }
        return value.toString();
    }
}
