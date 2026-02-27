package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.response.JwtResponse;
import com.Nguyen.blogplatform.payload.response.OAuthProfileResponse;
import com.Nguyen.blogplatform.repository.RoleRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthAuthService {

    private final OAuthProviderService oAuthProviderService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public OAuthProfileResponse verifyToken(String provider, String accessToken) {
        return oAuthProviderService.verifyAndFetchProfile(provider, accessToken);
    }

    @Transactional
    public ResponseEntity<JwtResponse> loginOrRegister(
        String provider,
        String accessToken
    ) {
        OAuthProfileResponse profile =
            oAuthProviderService.verifyAndFetchProfile(provider, accessToken);

        User user = userRepository
            .findByEmail(profile.email())
            .map(existing -> updateExisting(existing, profile))
            .orElseGet(() -> createNew(profile));

        String jwtToken = jwtUtils.generateTokenFromUserId(user.getId(), user.getEmail());
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

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
            .body(jwtResponse);
    }

    private User updateExisting(User existing, OAuthProfileResponse profile) {
        existing.setAuthProvider(profile.provider());
        existing.setProviderId(profile.providerId());
        if (profile.avatar() != null && !profile.avatar().isBlank()) {
            existing.setAvatar(profile.avatar());
        }
        return userRepository.save(existing);
    }

    private User createNew(OAuthProfileResponse profile) {
        User user = new User();
        user.setEmail(profile.email());
        user.setUsername(generateUsername(profile.name(), profile.email()));
        user.setPassword(passwordEncoder.encode(generatePasswordSeed()));
        user.setAvatar(profile.avatar());
        user.setAuthProvider(profile.provider());
        user.setProviderId(profile.providerId());

        Role userRole = roleRepository
            .findByName(ERole.ROLE_USER)
            .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));
        user.setRoles(Set.of(userRole));

        return userRepository.save(user);
    }

    private String generateUsername(String name, String email) {
        String base = (name != null && !name.isBlank())
            ? name.replaceAll("\\s+", "").toLowerCase()
            : email.substring(0, email.indexOf('@'));

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private String generatePasswordSeed() {
        return "Oauth2" + UUID.randomUUID() + "Aa1!";
    }
}
