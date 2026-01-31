package com.sixgroup.refit.ejemplo;

import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     * 1️⃣ Inicialización de infraestructura RabbitMQ al arrancar.
     * Esto asegura que el Exchange, la Cola y el Binding existan antes de recibir peticiones.
     */
    @Bean
    public ApplicationRunner initializeRabbit(
            RabbitAdmin rabbitAdmin,
            Queue invitationQueue,
            TopicExchange invitationExchange,
            Binding invitationBinding) {

        return args -> {
            try {
                log.info("Checking RabbitMQ infrastructure...");
                rabbitAdmin.declareExchange(invitationExchange);
                rabbitAdmin.declareQueue(invitationQueue);
                rabbitAdmin.declareBinding(invitationBinding);
                log.info("✅ RabbitMQ: Exchange y Queue declarados correctamente.");
            } catch (Exception e) {
                log.error("❌ ERROR: No se pudo conectar con RabbitMQ. " +
                        "Asegúrate de que el servicio esté corriendo en tu PC local.");
            }
        };
    }

    /**
     * 2️⃣ Inicialización de usuarios por defecto en Base de Datos.
     */
    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfNotExists(userRepository, passwordEncoder, "admin@test.com", "Super Admin", "admin123", Role.ADMIN);
            createUserIfNotExists(userRepository, passwordEncoder, "user1@test.com", "User One", "user123", Role.USER);
            createUserIfNotExists(userRepository, passwordEncoder, "user2@test.com", "User Two", "user123", Role.USER);
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
            log.info("👤 Usuario Creado -> Email: {}, Rol: {}", email, role);
        } else {
            log.info("ℹ️ Usuario ya existente: {}", email);
        }
    }
}