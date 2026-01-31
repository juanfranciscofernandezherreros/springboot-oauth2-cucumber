package com.sixgroup.refit.ejemplo.config;

import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class TestAdminUsersInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {

        log.info("🚀 [TEST] Inicializando usuarios de test");

        // =====================
        // ADMINS
        // =====================
        createAdminIfNotExists("admin1@test.com", "Admin123!", "Admin One");
        createAdminIfNotExists("admin2@test.com", "Admin123!", "Admin Two");

        // =====================
        // USER NORMAL (LOGIN OK)
        // =====================
        createUserIfNotExists(
                "usuario@login.com",
                "passLogin123",
                "Usuario Login"
        );
        createUserIfNotExists(
                "user@test.com",
                "123",
                "UsuarioQueVaAserActualizado"
        );

        createUserIfNotExists(
                "fresh_user@test.com",
                "123",
                "UserToTestLogout"
        );

        createUserIfNotExists(
                "reset_test@test.com",
                "PassActual123",
                "ResetPassword"
        );

        createUserIfNotExists(
                "usuario_profile_1@test.com",
                "passLogin123",
                "PassLogin123"
        );

        createUserIfNotExists(
                "usuario_paraborrar@login.com",
                "passLogin123",
                "PassLogin123"
        );

        createUserIfNotExists(
                "active.user@test.com",
                "passLogin123",
                "ActiveUser"
        );



        // =====================
        // USER BLOQUEADO
        // =====================
        createLockedUserIfNotExists(
                "locked.user@test.com",
                "User123!",
                "Locked User"
        );

        User userToUpdate = createUserIfNotExistsAndReturn(
                "user_to_update@test.com",
                "123",
                "UsuarioParaActualizar"
        );

        log.info("🆔 [TEST] ID usuario para actualizar: {}", userToUpdate.getId());

        log.info("✅ [TEST] Inicialización de usuarios finalizada");

        User userToDelete = createUserIfNotExistsAndReturn(
                "userstodelete@test.com",
                "123",
                "UsuarioParaBorrar"
        );

        log.info("🗑️ [TEST] ID usuario para borrar: {} {} ", userToDelete.getId() , userToDelete.getEmail());

        createUserIfNotExists(
                "profile_user@test.com",
                "123",
                "UsuarioPerfil"
        );


    }

    // =====================================================
    // ADMIN
    // =====================================================
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
        log.info("✅ [TEST] Admin creado: {}", email);
    }

    // =====================================================
    // USER NORMAL (NO BLOQUEADO)
    // =====================================================
    private void createUserIfNotExists(String email, String rawPassword, String name) {

        if (userRepository.existsByEmail(email)) {
            log.info("ℹ️ [TEST] Usuario ya existe: {}", email);
            return;
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.USER)
                .failedAttempt(0)
                .lockCount(0)
                .accountNonLocked(true)
                .lockTime(null)
                .build();

        userRepository.save(user);
        log.info("👤 [TEST] Usuario creado: {}", email);
    }

    // =====================================================
    // USER BLOQUEADO
    // =====================================================
    private void createLockedUserIfNotExists(String email, String rawPassword, String name) {

        if (userRepository.existsByEmail(email)) {
            log.info("ℹ️ [TEST] Usuario bloqueado ya existe: {}", email);
            return;
        }

        User lockedUser = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.USER)
                .failedAttempt(5)
                .lockCount(1)
                .accountNonLocked(false)
                .lockTime(new Date())
                .build();

        userRepository.save(lockedUser);
        log.info("🔒 [TEST] Usuario bloqueado creado: {}", email);
    }

    private User createUserIfNotExistsAndReturn(String email, String rawPassword, String name) {

        return userRepository.findByEmail(email).orElseGet(() -> {

            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(Role.USER)
                    .failedAttempt(0)
                    .lockCount(0)
                    .accountNonLocked(true)
                    .lockTime(null)
                    .build();

            User saved = userRepository.save(user);
            log.info("👤 [TEST] Usuario creado para update: {} (id={})", email, saved.getId());
            return saved;
        });
    }

}
