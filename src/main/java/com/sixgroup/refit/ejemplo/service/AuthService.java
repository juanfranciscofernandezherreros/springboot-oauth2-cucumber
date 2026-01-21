package com.sixgroup.refit.ejemplo.service;

import com.sixgroup.refit.ejemplo.controller.*;
import com.sixgroup.refit.ejemplo.exceptions.InvalidRoleForRegistrationException;
import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.usuario.User;
import com.sixgroup.refit.ejemplo.repository.Token;
import com.sixgroup.refit.ejemplo.repository.TokenRepository;
import com.sixgroup.refit.ejemplo.usuario.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private final IpLockService ipLockService;

    private long getLockDuration(int lockCount) {
        return switch (lockCount) {
            case 1 -> 1 * 60 * 1000;
            case 2 -> 2 * 60 * 1000;
            case 3 -> 3 * 60 * 1000;
            default -> -1L;
        };
    }

    // AÑADIDO: clientIp para validar antes de crear nada
    // AÑADIDO: clientIp para validar antes de crear nada
    @Transactional
    public void register(RegisterRequest request, String clientIp) {

        if (ipLockService.isIpBlocked(clientIp)) {
            throw new LockedException("Tu IP está bloqueada. No puedes registrar cuentas.");
        }

        if (request.role() != Role.USER) {
            throw new InvalidRoleForRegistrationException();
        }

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER) // aquí ya es seguro
                .accountNonLocked(true)
                .failedAttempt(0)
                .lockCount(0)
                .build();

        userRepository.save(user);
    }

    // En AuthService.java

    // 1. Método para la WEB (Público)
    @Transactional
    public void registerPublic(RegisterRequest request, String clientIp) {
        // Validar IP...
        if (ipLockService.isIpBlocked(clientIp)) throw new LockedException("IP Bloqueada");

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER) // <--- SIEMPRE USER
                .accountNonLocked(true)
                .failedAttempt(0)
                .lockCount(0)
                .build();

        userRepository.save(user);
    }

    // 2. Método para el PANEL ADMIN (Privado)
    @Transactional
    public void registerByAdmin(AdminCreateUserRequest request) {
        // Aquí NO validamos IP lock del admin (se supone que es de confianza),
        // pero podrías validarlo si quieres.

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .accountNonLocked(true)
                .failedAttempt(0)
                .lockCount(0)
                .build();

        userRepository.save(user);
        log.info("ADMIN creó un nuevo usuario: {} con rol {}", request.email(), request.role());
    }

    public TokenResponse login(LoginRequest request, String clientIp) {

        // 1. Validación preventiva de IP
        if (ipLockService.isIpBlocked(clientIp)) {
            throw new LockedException("Tu IP ha sido bloqueada temporalmente por seguridad.");
        }

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    // Si no existe usuario, castigamos la IP (evita escaneo de emails)
                    ipLockService.registerFailedAttempt(clientIp);
                    return new BadCredentialsException("Usuario no encontrado");
                });

        if (!user.isAccountNonLocked()) {
            long duration = getLockDuration(user.getLockCount());
            if (duration == -1L) {
                throw new LockedException("Cuenta bloqueada permanentemente. Contacte a soporte.");
            }
            if (shouldUnlock(user)) {
                unlockUser(user);
            } else {
                long timeLeft = (user.getLockTime().getTime() + duration) - System.currentTimeMillis();
                throw new LockedException("Cuenta bloqueada. Intenta en " + (timeLeft / 1000) + " segundos.");
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            resetAllAttempts(user);
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);

            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);
            saveUserToken(user, refreshToken);

            return new TokenResponse(jwtToken, refreshToken);

        } catch (BadCredentialsException e) {
            // Fallo de contraseña: penalizamos cuenta Y penalizamos IP
            updateFailedAttempts(request.email());
            ipLockService.registerFailedAttempt(clientIp);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFailedAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(this::processFailedAttempt);
    }

    @Transactional
    public void unlockUser(User user) {
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        userRepository.save(user);
    }

    // AÑADIDO: clientIp como parámetro
    @Transactional
    public void resetPassword(ResetPasswordRequest request, String clientIp) {
        // 1. Validación de IP primero
        if (ipLockService.isIpBlocked(clientIp)) { // <--- NUEVO
            throw new LockedException("Tu IP está bloqueada.");
        }

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    // Si intentan cambiar password de un email que NO existe -> Castigo a la IP
                    ipLockService.registerFailedAttempt(clientIp); // <--- NUEVO
                    // Importante: Lanzamos excepción genérica o BadCredentials para no dar pistas
                    return new RuntimeException("Solicitud inválida");
                });

        if (!user.isAccountNonLocked()) {
            log.error("SEGURIDAD: Intento de reset de password en cuenta bloqueada: {}", user.getEmail());
            // Si la cuenta está bloqueada, también sumamos un strike a la IP por insistente
            ipLockService.registerFailedAttempt(clientIp); // <--- NUEVO (Opcional)

            throw new LockedException("OPERACIÓN DENEGADA: La cuenta se encuentra bloqueada.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        revokeAllUserTokens(user);
        userRepository.save(user);
        log.info("ÉXITO: Contraseña actualizada para el usuario {}", user.getEmail());
    }

    // --- MÉTODOS PRIVADOS ---
    // (Igual que antes...)
    private void processFailedAttempt(User user) {
        int newAttempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(newAttempts);

        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setLockTime(new Date());
            user.setLockCount(user.getLockCount() + 1);
            user.setFailedAttempt(0);
            log.warn("Usuario {} BLOQUEADO - Nivel {}", user.getEmail(), user.getLockCount());
        }
        userRepository.saveAndFlush(user);
    }

    // (Resto de métodos privados igual: shouldUnlock, resetAllAttempts, saveUserToken, revokeAllUserTokens...)
    private boolean shouldUnlock(User user) {
        if (user.getLockTime() == null) return true;
        long duration = getLockDuration(user.getLockCount());
        if (duration == -1L) return false;
        return (user.getLockTime().getTime() + duration) < System.currentTimeMillis();
    }

    private void resetAllAttempts(User user) {
        user.setFailedAttempt(0);
        user.setLockCount(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        userRepository.save(user);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    @Transactional
    public TokenResponse refreshToken(String authHeader) {
        // Nota: Podrías añadir validación de IP aquí también si quieres máxima seguridad
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        final String refreshToken = authHeader.substring(7);
        try {
            String userEmail = jwtService.extractUsername(refreshToken);
            if (userEmail != null) {
                var user = userRepository.findByEmail(userEmail).orElseThrow();
                var storedToken = tokenRepository.findByToken(refreshToken).orElse(null);

                if (storedToken != null && !storedToken.isRevoked() && jwtService.isTokenValid(refreshToken, user)) {
                    revokeAllUserTokens(user);
                    var accessToken = jwtService.generateToken(user);
                    var newRefreshToken = jwtService.generateRefreshToken(user);
                    saveUserToken(user, accessToken);
                    saveUserToken(user, newRefreshToken);
                    return new TokenResponse(accessToken, newRefreshToken);
                }
            }
        } catch (Exception e) {
            log.error("Error en proceso de Refresh");
        }
        return null;
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return;
        final String jwt = authHeader.substring(7);
        tokenRepository.findByToken(jwt).ifPresent(token -> {
            token.setExpired(true);
            token.setRevoked(true);
            tokenRepository.save(token);
            SecurityContextHolder.clearContext();
            log.info("Cierre de sesión: Token invalidado.");
        });
    }
}