package com.Nguyen.blogplatform.domain.auth.application;



import com.Nguyen.blogplatform.domain.user.domain.model.Role;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.RoleRepository;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import com.Nguyen.blogplatform.infrastructure.security.oauth2.OAuth2UserInfo;
import com.Nguyen.blogplatform.infrastructure.security.oauth2.OAuth2UserInfoFactory;
import com.Nguyen.blogplatform.shared.enums.ERole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        if (!StringUtils.hasText(userInfo.getEmail())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"),
                    "OAuth provider did not return an email address."
            );
        }

        User user = upsertUser(userInfo, registrationId);

        attributes.put("resolved_email", user.getEmail());
        attributes.put("resolved_user_id", user.getId());

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());

        log.info("OAuth2 login processed for provider={} userId={}", registrationId, user.getId());

        return new DefaultOAuth2User(authorities, attributes, "resolved_email");
    }

    private User upsertUser(OAuth2UserInfo userInfo, String provider) {
        String providerKey = provider.toUpperCase();

        return userRepository.findByEmail(userInfo.getEmail())
                .map(existing -> linkExistingUser(existing, userInfo, providerKey))
                .orElseGet(() -> createOAuthUser(userInfo, providerKey));
    }

    private User linkExistingUser(User existing, OAuth2UserInfo userInfo, String provider) {
        existing.setAuthProvider(provider);
        existing.setProviderId(userInfo.getId());
        if (userInfo.getImageUrl() != null) {
            existing.setAvatar(userInfo.getImageUrl());
        }
        return userRepository.save(existing);
    }

    private User createOAuthUser(OAuth2UserInfo userInfo, String provider) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setUsername(generateUsername(userInfo.getName(), userInfo.getEmail()));
        user.setPassword(passwordEncoder.encode("Oauth2" + UUID.randomUUID() + "Aa1!"));
        user.setAvatar(userInfo.getImageUrl());
        user.setAuthProvider(provider);
        user.setProviderId(userInfo.getId());

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));
        user.setRoles(Set.of(userRole));

        return userRepository.save(user);
    }

    private String generateUsername(String name, String email) {
        String base = StringUtils.hasText(name)
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
}
