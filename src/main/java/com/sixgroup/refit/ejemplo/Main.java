package com.sixgroup.refit.ejemplo;

import com.sixgroup.refit.ejemplo.model.Invitation;
import com.sixgroup.refit.ejemplo.model.InvitationStatus;
import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.repository.InvitationRepository;
import com.sixgroup.refit.ejemplo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     * Inicialización de datos por defecto: 10 Usuarios y 12 Invitaciones.
     */
    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      InvitationRepository invitationRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("🚀 Iniciando carga de datos de prueba...");

            // --- 1. CREACIÓN DE 10 USUARIOS ---

            // 2 Administradores
            createUser(userRepository, passwordEncoder, "admin@test.com", "Super Admin", "admin123", Role.ADMIN, false);
            createUser(userRepository, passwordEncoder, "admin2@test.com", "Secondary Admin", "admin123", Role.ADMIN, false);

            // 2 Managers
            createUser(userRepository, passwordEncoder, "manager1@test.com", "Project Manager", "manager123", Role.MANAGER, false);
            createUser(userRepository, passwordEncoder, "manager2@test.com", "HR Manager", "manager123", Role.MANAGER, false);

            // 6 Usuarios (El primero estará bloqueado para pruebas de stats)
            createUser(userRepository, passwordEncoder, "user1@test.com", "User Blocked", "user123", Role.USER, true);
            createUser(userRepository, passwordEncoder, "user2@test.com", "User Active", "user123", Role.USER, false);
            createUser(userRepository, passwordEncoder, "user3@test.com", "User Three", "user123", Role.USER, false);
            createUser(userRepository, passwordEncoder, "user4@test.com", "User Four", "user123", Role.USER, false);
            createUser(userRepository, passwordEncoder, "user5@test.com", "User Five", "user123", Role.USER, false);
            createUser(userRepository, passwordEncoder, "user6@test.com", "User Six", "user123", Role.USER, false);

            // --- 2. CREACIÓN DE 12 INVITACIONES ---
            if (invitationRepository.count() == 0) {
                createSampleInvitations(invitationRepository);
            }

            log.info("✅ Carga de datos completada con éxito.");
        };
    }

    /**
     * Lógica para crear un usuario si no existe.
     */
    private void createUser(UserRepository repo, PasswordEncoder encoder, String email,
                            String name, String rawPass, Role role, boolean isLocked) {
        if (repo.findByEmail(email).isEmpty()) {
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(encoder.encode(rawPass))
                    .role(role)
                    .accountNonLocked(!isLocked) // true si NO está bloqueado
                    .failedAttempt(0)
                    .lockCount(isLocked ? 1 : 0)
                    .build();
            repo.save(user);
            log.info("👤 Usuario creado: {} [{}] (Bloqueado: {})", email, role, isLocked);
        }
    }

    /**
     * Lógica para crear 12 invitaciones con estados variados.
     */
    private void createSampleInvitations(InvitationRepository repo) {
        InvitationStatus[] statuses = InvitationStatus.values();

        for (int i = 1; i <= 12; i++) {
            // Rotamos circularmente por los estados del Enum (PENDING, ACCEPTED, etc.)
            InvitationStatus status = statuses[(i - 1) % statuses.length];

            Invitation invitation = Invitation.builder()
                    .email("invite" + i + "@external.com")
                    .name("Persona Invitada " + i)
                    .description("Invitación automática de prueba número " + i)
                    .role(Role.USER)
                    .token(UUID.randomUUID().toString())
                    .status(status)
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();

            repo.save(invitation);
        }
        log.info("📧 12 Invitaciones generadas con diversos estados.");
    }
}