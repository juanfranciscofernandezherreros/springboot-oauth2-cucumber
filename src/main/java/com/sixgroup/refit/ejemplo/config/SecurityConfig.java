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
                /* =========================
                   CORS & CSRF
                   ========================= */
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                /* =========================
                   SESSION (JWT = STATELESS)
                   ========================= */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /* =========================
                   AUTHORIZATION
                   ========================= */
                .authorizeHttpRequests(auth -> auth

                        // 1. Recursos Públicos y Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/static/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/auth/**",
                                "/ws-invitations/**"
                        ).permitAll()

                        // 2. Invitaciones (Configuración específica)
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/invitations").permitAll()
                        .requestMatchers("/api/v1/admin/invitations/**").hasRole("ADMIN")

                        // 3. 🔒 GESTIÓN DE PERFIL (Lo que pediste)
                        // Solo el rol USER puede editar su perfil
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/update").hasRole("USER")
                        // Consultar el perfil requiere estar autenticado (cualquier rol)
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()

                        // 4. 🔒 ADMIN AREA
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 5. Resto de la API
                        .anyRequest().authenticated()
                )

                /* =========================
                   JWT & AUTH PROVIDER
                   ========================= */
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                /* =========================
                   LOGOUT
                   ========================= */
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                            response.setStatus(200);
                        })
                );

        return http.build();
    }

    /* =========================
       CORS CONFIG
       ========================= */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8087"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}