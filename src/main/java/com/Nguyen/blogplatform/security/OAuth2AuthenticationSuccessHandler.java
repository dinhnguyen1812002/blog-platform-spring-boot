package com.Nguyen.blogplatform.security;

import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.response.JwtResponse;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
    implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String userId = (String) oauth2User.getAttributes().get("resolved_user_id");
        String email = (String) oauth2User.getAttributes().get("resolved_email");

        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found"));

        String jwtToken = jwtUtils.generateTokenFromUserId(user.getId(), email);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        ResponseCookie jwtCookie = jwtUtils.generateCookieFromToken(jwtToken);
        ResponseCookie refreshTokenCookie = refreshTokenService
            .generateRefreshTokenCookie(refreshToken.getToken());

        List<String> roles = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toList());

        JwtResponse jwtResponse = new JwtResponse(
            jwtToken,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getSlug(),
            user.getAvatar(),
            roles
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        objectMapper.writeValue(response.getOutputStream(), jwtResponse);
    }
}
