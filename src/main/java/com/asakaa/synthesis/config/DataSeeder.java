package com.asakaa.synthesis.config;

import com.asakaa.synthesis.domain.entity.Provider;
import com.asakaa.synthesis.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.superadmin.email}")
    private String superAdminEmail;

    @Value("${app.superadmin.password}")
    private String superAdminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (!providerRepository.existsByEmail(superAdminEmail)) {
            log.info("Seeding default SUPER_ADMIN account...");
            Provider superAdmin = Provider.builder()
                    .name("System Super Admin")
                    .email(superAdminEmail)
                    .passwordHash(passwordEncoder.encode(superAdminPassword))
                    .role("SUPER_ADMIN")
                    .build();

            providerRepository.save(superAdmin);
            log.info("SUPER_ADMIN seeded with email: {}", superAdminEmail);
        } else {
            log.info("SUPER_ADMIN account already exists.");
        }
    }
}
