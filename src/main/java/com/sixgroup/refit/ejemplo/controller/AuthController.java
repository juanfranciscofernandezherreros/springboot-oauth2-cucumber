package com.sixgroup.refit.ejemplo.controller;

import com.sixgroup.refit.ejemplo.service.AuthService;
// El IpLockService ya no se usa directamente aquí en los catch, lo maneja el AuthService
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    // private final IpLockService ipLockService; // Ya no es necesario inyectarlo aquí si toda la lógica pasó al Service

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = httpRequest.getRemoteAddr();
        service.register(request, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticate(
            @RequestBody final LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = httpRequest.getRemoteAddr();
        // Pasamos la IP al servicio
        return ResponseEntity.ok(service.login(request, clientIp));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader
    ) {
        TokenResponse response = service.refreshToken(authHeader);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        service.logout(authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        try {
            // AHORA: Pasamos la IP. El servicio valida si la IP está bloqueada antes de nada.
            service.resetPassword(request, ip);
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada"));

        } catch (LockedException e) {
            // Si llega aquí, es porque el usuario está bloqueado O la IP está bloqueada.
            // El servicio ya se encargó de registrar el intento fallido en Redis/DB.
            // Solo devolvemos el error 423.
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of("error", e.getMessage()));

        } catch (RuntimeException e) {
            // Para otros errores (ej. usuario no encontrado genérico)
            return ResponseEntity.badRequest().body(Map.of("error", "Error al procesar la solicitud"));
        }
    }
}