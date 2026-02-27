package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.RoleRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.util.SpringContextUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService() {}

    public CustomOAuth2UserService(
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = SpringContextUtil.getBean(UserRepository.class);
        }
        return userRepository;
    }

    private RoleRepository getRoleRepository() {
        if (roleRepository == null) {
            roleRepository = SpringContextUtil.getBean(RoleRepository.class);
        }
        return roleRepository;
    }

    private PasswordEncoder getPasswordEncoder() {
        if (passwordEncoder == null) {
            passwordEncoder = SpringContextUtil.getBean(PasswordEncoder.class);
        }
        return passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
        throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest
            .getClientRegistration()
            .getRegistrationId();
        Map<String, Object> attributes = new HashMap<>(
            oauth2User.getAttributes()
        );

        String email = resolveEmail(registrationId, attributes);
        String providerId = resolveProviderId(registrationId, attributes);
        String name = resolveName(registrationId, attributes);
        String avatar = resolveAvatar(registrationId, attributes);

        User user = upsertUser(email, name, avatar, registrationId, providerId);

        attributes.put("resolved_email", user.getEmail());
        attributes.put("resolved_user_id", user.getId());

        return new DefaultOAuth2User(
            Set.of(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "resolved_email"
        );
    }

    private User upsertUser(
        String email,
        String name,
        String avatar,
        String provider,
        String providerId
    ) {
        return getUserRepository()
            .findByEmail(email)
            .map(existing -> updateExisting(existing, provider, providerId, avatar))
            .orElseGet(() -> createNew(email, name, avatar, provider, providerId));
    }

    private User updateExisting(
        User existing,
        String provider,
        String providerId,
        String avatar
    ) {
        existing.setAuthProvider(provider);
        existing.setProviderId(providerId);
        if (avatar != null && !avatar.isBlank()) {
            existing.setAvatar(avatar);
        }
        return getUserRepository().save(existing);
    }

    private User createNew(
        String email,
        String name,
        String avatar,
        String provider,
        String providerId
    ) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(generateUsername(name, email));
        user.setPassword(getPasswordEncoder().encode(generatePasswordSeed()));
        user.setAvatar(avatar);
        user.setAuthProvider(provider);
        user.setProviderId(providerId);

        Role userRole = getRoleRepository()
            .findByName(ERole.ROLE_USER)
            .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));
        user.setRoles(Set.of(userRole));

        return getUserRepository().save(user);
    }

    private String resolveEmail(String provider, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        if (email == null && "github".equals(provider)) {
            String login = (String) attributes.get("login");
            email = login + "@github.local";
        }
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email not provided by " + provider);
        }
        return email.toLowerCase(Locale.ROOT);
    }

    private String resolveProviderId(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("sub");
        }
        return String.valueOf(attributes.get("id"));
    }

    private String resolveName(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("name");
        }
        if ("github".equals(provider)) {
            String name = (String) attributes.get("name");
            return name != null ? name : (String) attributes.get("login");
        }
        return "oauth-user";
    }

    private String resolveAvatar(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("picture");
        }
        if ("github".equals(provider)) {
            return (String) attributes.get("avatar_url");
        }
        return null;
    }

    private String generateUsername(String name, String email) {
        String base = (name != null && !name.isBlank())
            ? name.replaceAll("\\s+", "").toLowerCase(Locale.ROOT)
            : email.substring(0, email.indexOf('@'));

        String candidate = base;
        int suffix = 1;
        while (getUserRepository().existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private String generatePasswordSeed() {
        return "Oauth2" + UUID.randomUUID() + "Aa1!";
    }
}
