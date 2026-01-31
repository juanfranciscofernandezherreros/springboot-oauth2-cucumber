package com.sixgroup.refit.ejemplo;

import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@test.com";

            // 1. Verificamos si ya existe para no duplicarlo
            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                // 2. Creamos el objeto Admin
                User admin = User.builder()
                        .name("Super Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin123")) // Importante: Encriptar password
                        .role(Role.ADMIN) // Rol ADMIN
                        .accountNonLocked(true)
                        .failedAttempt(0)
                        .lockCount(0)
                        .build();

                // 3. Guardamos en Base de Datos
                userRepository.save(admin);

                System.out.println("---------------------------------------------");
                System.out.println("✅ USUARIO ADMIN CREADO EXITOSAMENTE");
                System.out.println("📧 Email: " + adminEmail);
                System.out.println("🔑 Pass:  admin123");
                System.out.println("---------------------------------------------");
            } else {
                System.out.println("ℹ️ El usuario Admin ya existe, no se ha creado de nuevo.");
            }
        };
    }


}