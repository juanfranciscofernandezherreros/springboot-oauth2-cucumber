package com.sixgroup.refit.ejemplo.config;

import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.usuario.User;
import com.sixgroup.refit.ejemplo.usuario.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class TestAdminUsersInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {

        log.info("🚀 [TEST] Inicializando usuarios ADMIN de test");

        createAdminIfNotExists("admin1@test.com", "Admin123!", "Admin One");
        createAdminIfNotExists("admin2@test.com", "Admin123!", "Admin Two");

        log.info("✅ [TEST] Inicialización de usuarios ADMIN finalizada");
    }

    private void createAdminIfNotExists(String email, String rawPassword, String name) {

        if (userRepository.existsByEmail(email)) {
            log.info("ℹ️ [TEST] Admin ya existe: {}", email);
            return;
        }

        User admin = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ADMIN)
                .failedAttempt(0)
                .lockCount(0)
                .accountNonLocked(true)
                .lockTime(null)
                .build();

        userRepository.save(admin);

        log.info("✅ [TEST] Admin creado correctamente: {}", email);
    }
}
