package com.Nguyen.blogplatform.seed;

import org.springframework.boot.CommandLineRunner;
import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.RoleRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InitRole implements CommandLineRunner {


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🔍 Checking admin existence...");

        // 1️⃣ Nếu đã có ADMIN → dừng
        if (userRepository.existsByRoles_Name(ERole.ROLE_ADMIN)) {
            log.info("Admin already exists. Skipping admin initialization.");
            return;
        }

        // 2️⃣ Lấy role ADMIN
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        // 3️⃣ Tạo ADMIN đầu tiên
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@blogplatform.com");
        admin.setPassword(passwordEncoder.encode("kJ35&z7#i%=X")); //
        admin.setRoles(Set.of(adminRole));


        userRepository.save(admin);

        log.warn("DEFAULT ADMIN CREATED ");
        log.warn("Username: admin");
        log.warn("Password: kJ35&z7#i%=X");
        log.warn("PLEASE CHANGE PASSWORD IMMEDIATELY");
    }
}
