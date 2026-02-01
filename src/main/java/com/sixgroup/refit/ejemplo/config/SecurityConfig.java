package com.sixgroup.refit.ejemplo.config;

import com.sixgroup.refit.ejemplo.service.LogoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                // =========================
                // CORS & CSRF
                // =========================
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // =========================
                // SESSION (JWT = STATELESS)
                // =========================
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // =========================
                // AUTHORIZATION
                // =========================
                .authorizeHttpRequests(auth -> auth

                        // 1. Preflight (OPTIONS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Swagger / OpenAPI / Recursos Estáticos
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/static/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 3. Auth pública
                        .requestMatchers("/auth/**").permitAll()

                        // 4. 🔥 WEBSOCKETS (Permitir Handshake de STOMP y SockJS)
                        .requestMatchers("/ws-invitations/**").permitAll()

                        // 5. INVITACIONES PÚBLICAS (Endpoint que probaste con cURL)
                        .requestMatchers(HttpMethod.POST, "/api/v1/invitations").permitAll()

                        // 6. INVITACIONES ADMIN (Protegidas por Rol)
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/invitations",
                                "/api/v1/admin/invitations/stream"
                        ).hasRole("ADMIN")

                        // 7. Resto de la API Admin
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 8. Cualquier otra petición requiere login
                        .anyRequest().authenticated()
                )

                // =========================
                // AUTH PROVIDER + JWT FILTER
                // =========================
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // =========================
                // LOGOUT (Ajustado para 401/Success)
                // =========================
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                            response.setStatus(200); // O 401 si prefieres forzar re-login
                        })
                );

        return http.build();
    }

    // =========================
    // CORS CONFIGURATION
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Permitimos localhost:3000 (React) y localhost:8087 (Propio backend)
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8087"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));

        // Permitir credenciales es vital para SockJS
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}