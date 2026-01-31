package com.sixgroup.refit.ejemplo;

import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     * Inicialización de usuarios por defecto.
     */
    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder) {

        return args -> {

            // ======================
            // ADMIN
            // ======================
            createUserIfNotExists(
                    userRepository,
                    passwordEncoder,
                    "admin@test.com",
                    "Super Admin",
                    "admin123",
                    Role.ADMIN
            );

            // ======================
            // USERS
            // ======================
            createUserIfNotExists(
                    userRepository,
                    passwordEncoder,
                    "user1@test.com",
                    "User One",
                    "user123",
                    Role.USER
            );

            createUserIfNotExists(
                    userRepository,
                    passwordEncoder,
                    "user2@test.com",
                    "User Two",
                    "user123",
                    Role.USER
            );
        };
    }

    private void createUserIfNotExists(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       String email,
                                       String name,
                                       String rawPassword,
                                       Role role) {

        if (userRepository.findByEmail(email).isEmpty()) {

            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .accountNonLocked(true)
                    .failedAttempt(0)
                    .lockCount(0)
                    .build();

            userRepository.save(user);

            System.out.println("---------------------------------------------");
            System.out.println("✅ USUARIO CREADO");
            System.out.println("👤 Rol:   " + role);
            System.out.println("📧 Email: " + email);
            System.out.println("🔑 Pass:  " + rawPassword);
            System.out.println("---------------------------------------------");

        } else {
            System.out.println("ℹ️ Usuario ya existente: " + email);
        }
    }
}
