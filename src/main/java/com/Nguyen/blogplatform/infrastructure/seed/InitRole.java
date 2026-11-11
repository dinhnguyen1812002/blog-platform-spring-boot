package com.Nguyen.blogplatform.infrastructure.seed;

import com.Nguyen.blogplatform.domain.user.domain.model.Role;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.RoleRepository;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import com.Nguyen.blogplatform.shared.enums.ERole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Startup seed (idempotent):
 *  1. ensures every canonical role in {@link ERole} exists
 *  2. creates the first admin account if there is none
 *
 * The admin password comes from ADMIN_INITIAL_PASSWORD; if unset, a random one is
 * generated and printed once. Never hard-code credentials in source.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class InitRole implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_INITIAL_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_INITIAL_EMAIL:admin@blogplatform.com}")
    private String adminEmail;

    @Value("${ADMIN_INITIAL_PASSWORD:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedAdmin();
    }

    private void seedRoles() {
        for (ERole name : ERole.values()) {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(new Role(name));
                log.info("Seeded role {}", name);
            }
        }
    }

    private void seedAdmin() {
        if (userRepository.existsByRoles_Name(ERole.ROLE_ADMIN)) {
            log.info("Admin already exists. Skipping admin initialization.");
            return;
        }

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow();

        boolean generated = adminPassword == null || adminPassword.isBlank();
        String password = generated ? generatePassword() : adminPassword;

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRoles(new HashSet<>(Set.of(adminRole, userRole)));
        userRepository.save(admin);

        if (generated) {
            // Printed once on first boot only; set ADMIN_INITIAL_PASSWORD to avoid this.
            log.warn("DEFAULT ADMIN CREATED — username: {} — generated password: {}", adminUsername, password);
            log.warn("Change this password immediately.");
        } else {
            log.warn("DEFAULT ADMIN CREATED — username: {} (password from ADMIN_INITIAL_PASSWORD)", adminUsername);
        }
    }

    private static String generatePassword() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        // Base64url + a fixed suffix so it satisfies the User.password complexity rule
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) + "aA1@";
    }
}
