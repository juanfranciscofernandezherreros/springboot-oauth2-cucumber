package com.sixgroup.refit.ejemplo.config;

import com.sixgroup.refit.ejemplo.service.LogoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.sixgroup.refit.ejemplo.model.Role.ADMIN; // Importa tu ENUM si es necesario

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutService logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. PÚBLICO: Login, Registro de usuarios normales, Reset Password
                        .requestMatchers("/auth/**").permitAll()

                        // 2. PRIVADO (SOLO ADMIN): Crear usuarios avanzados, ver logs, desbloquear, etc.
                        // Nota: hasRole("ADMIN") espera que la autoridad sea "ROLE_ADMIN".
                        // Si tu sistema guarda solo "ADMIN", usa .hasAuthority("ADMIN") en su lugar.
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 3. RESTO: Requiere estar logueado (Cualquier rol: USER o ADMIN)
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) ->
                                SecurityContextHolder.clearContext()
                        )
                );

        return http.build();
    }
}