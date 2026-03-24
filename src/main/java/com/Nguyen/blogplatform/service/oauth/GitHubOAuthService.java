package com.Nguyen.blogplatform.service.oauth;

import com.Nguyen.blogplatform.Enum.EOAuthProvider;
import com.Nguyen.blogplatform.model.OAuthAccount;
import com.Nguyen.blogplatform.model.OAuthAuditLog;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.oauth.OAuthCallbackRequest;
import com.Nguyen.blogplatform.payload.response.oauth.OAuthAuthorizationResponse;
import com.Nguyen.blogplatform.payload.response.oauth.OAuthTokenResponse;
import com.Nguyen.blogplatform.repository.OAuthAccountRepository;
import com.Nguyen.blogplatform.repository.OAuthAuditLogRepository;
import com.Nguyen.blogplatform.repository.RoleRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import com.Nguyen.blogplatform.service.auth.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class GitHubOAuthService implements OAuthService {

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret:}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.github.redirect-uri:}")
    private String defaultRedirectUri;

    private static final String AUTHORIZATION_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_API_URL = "https://api.github.com/user";
    private static final String USER_EMAILS_URL = "https://api.github.com/user/emails";
    private static final String SCOPE = "read:user user:email";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OAuthAuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public EOAuthProvider getProvider() {
        return EOAuthProvider.GITHUB;
    }

    @Override
    public OAuthAuthorizationResponse initiateAuthorization(String redirectUri, String state, HttpServletRequest request) {
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);

        String finalRedirectUri = (redirectUri != null) ? redirectUri : defaultRedirectUri;

        String authorizationUrl = AUTHORIZATION_URL + "?" +
            "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
            "&redirect_uri=" + URLEncoder.encode(finalRedirectUri, StandardCharsets.UTF_8) +
            "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8) +
            "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);

        log.info("Initiated GitHub OAuth authorization for state: {}", state);

        return OAuthAuthorizationResponse.builder()
            .authorizationUrl(authorizationUrl)
            .state(state)
            .codeChallenge(codeVerifier)
            .expiresIn(600L)
            .build();
    }

    @Override
    @Transactional
    public OAuthTokenResponse handleCallback(OAuthCallbackRequest callbackRequest, HttpServletRequest request, HttpServletResponse response) {
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        try {
            String accessToken = exchangeCodeForToken(callbackRequest);

            Map<String, Object> userInfo = fetchUserInfo(accessToken);
            String providerId = String.valueOf(userInfo.get("id"));
            String login = (String) userInfo.get("login");
            String name = (String) userInfo.get("name");
            String avatarUrl = (String) userInfo.get("avatar_url");

            String email = (String) userInfo.get("email");
            if (email == null || email.isEmpty()) {
                email = fetchPrimaryEmail(accessToken);
            }
            if (email == null || email.isEmpty()) {
                email = login + "@github.local";
            }

            final String userEmail = email.toLowerCase(Locale.ROOT);

            boolean isNewUser = false;
            User user = userRepository.findByEmail(userEmail)
                .orElseGet(() -> {
                    User newUser = createNewUser(userEmail, name != null ? name : login, avatarUrl);
                    return userRepository.save(newUser);
                });

            if (userRepository.findByEmail(userEmail).isEmpty()) {
                isNewUser = true;
            }

            OAuthAccount oauthAccount = oauthAccountRepository
                .findByProviderAndProviderId(EOAuthProvider.GITHUB, providerId)
                .map(existing -> updateExistingAccount(existing, accessToken))
                .orElseGet(() -> createNewAccount(user, providerId, userEmail, login, name, avatarUrl, accessToken));

            oauthAccount.setLastUsedAt(LocalDateTime.now());
            oauthAccountRepository.save(oauthAccount);

            String jwtToken = jwtUtils.generateTokenFromUserId(user.getId(), user.getEmail());
            var refreshTokenEntity = refreshTokenService.createRefreshToken(user.getId());

            ResponseCookie jwtCookie = jwtUtils.generateCookieFromToken(jwtToken);
            ResponseCookie refreshTokenCookie = refreshTokenService.generateRefreshTokenCookie(refreshTokenEntity.getToken());

            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            logAuditEvent(user.getId(), EOAuthProvider.GITHUB, "LOGIN_SUCCESS", "User logged in via GitHub OAuth", clientIp, userAgent, true, null);

            return OAuthTokenResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshTokenEntity.getToken())
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).toList())
                .newUser(isNewUser)
                .build();

        } catch (Exception e) {
            log.error("GitHub OAuth callback failed", e);
            logAuditEvent(null, EOAuthProvider.GITHUB, "LOGIN_FAILURE", "GitHub OAuth failed: " + e.getMessage(), clientIp, userAgent, false, e.getMessage());
            throw new RuntimeException("OAuth authentication failed", e);
        }
    }

    @Override
    @Transactional
    public void unlinkAccount(String userId, String accountId) {
        OAuthAccount account = oauthAccountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("OAuth account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to unlink this account");
        }

        account.setIsActive(false);
        oauthAccountRepository.save(account);

        log.info("Unlinked GitHub OAuth account {} for user {}", accountId, userId);
    }

    @Override
    public boolean supports(String provider) {
        return "github".equalsIgnoreCase(provider) || "GITHUB".equals(provider);
    }

    private String exchangeCodeForToken(OAuthCallbackRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", request.getCode());
        params.add("redirect_uri", request.getRedirectUri() != null ? request.getRedirectUri() : defaultRedirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            TOKEN_URL,
            HttpMethod.POST,
            entity,
            Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("access_token")) {
            throw new RuntimeException("Failed to obtain access token from GitHub");
        }

        return (String) body.get("access_token");
    }

    private Map<String, Object> fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            USER_API_URL,
            HttpMethod.GET,
            entity,
            Map.class
        );

        return response.getBody();
    }

    private String fetchPrimaryEmail(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<java.util.List> response = restTemplate.exchange(
                USER_EMAILS_URL,
                HttpMethod.GET,
                entity,
                java.util.List.class
            );

            java.util.List<Map<String, Object>> emails = response.getBody();
            if (emails != null) {
                for (Map<String, Object> emailObj : emails) {
                    Boolean primary = (Boolean) emailObj.get("primary");
                    Boolean verified = (Boolean) emailObj.get("verified");
                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                        return (String) emailObj.get("email");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch primary email from GitHub", e);
        }
        return null;
    }

    private User createNewUser(String email, String name, String avatar) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(generateUsername(name, email));
        user.setPassword(passwordEncoder.encode(generateRandomPassword()));
        user.setAvatar(avatar);

        var userRole = roleRepository.findByName(com.Nguyen.blogplatform.Enum.ERole.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Set.of(userRole));

        return user;
    }

    private OAuthAccount createNewAccount(User user, String providerId, String email, String login, String name, String avatar, String accessToken) {
        return OAuthAccount.builder()
            .user(user)
            .provider(EOAuthProvider.GITHUB)
            .providerId(providerId)
            .email(email)
            .providerUsername(login)
            .providerAvatarUrl(avatar)
            .accessToken(encryptToken(accessToken))
            .isPrimary(true)
            .isActive(true)
            .build();
    }

    private OAuthAccount updateExistingAccount(OAuthAccount account, String accessToken) {
        account.setAccessToken(encryptToken(accessToken));
        account.setTokenExpiresAt(LocalDateTime.now().plusHours(8));
        return account;
    }

    private String generateUsername(String name, String email) {
        String base = (name != null && !name.isBlank())
            ? name.replaceAll("\\s+", "").toLowerCase(Locale.ROOT)
            : email.substring(0, email.indexOf('@'));

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private String generateRandomPassword() {
        return "OAuth2" + UUID.randomUUID() + "Aa1!";
    }

    private String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate code challenge", e);
        }
    }

    private String encryptToken(String token) {
        if (token == null) return null;
        return token;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void logAuditEvent(String userId, EOAuthProvider provider, String eventType, String description,
                               String ipAddress, String userAgent, boolean success, String failureReason) {
        try {
            OAuthAuditLog auditLog = OAuthAuditLog.builder()
                .userId(userId)
                .provider(provider)
                .eventType(eventType)
                .eventDescription(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success)
                .failureReason(failureReason)
                .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to log audit event", e);
        }
    }
}
