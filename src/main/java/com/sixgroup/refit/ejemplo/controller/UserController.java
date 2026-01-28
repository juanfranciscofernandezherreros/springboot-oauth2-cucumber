package com.sixgroup.refit.ejemplo.controller;

import com.sixgroup.refit.ejemplo.dto.UpdateUserRequest;
import com.sixgroup.refit.ejemplo.dto.UserResponse;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Ver mi propio perfil
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserStatus(userDetails.getUsername());

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }

    // Actualizar mis propios datos -> AHORA DEVUELVE UserResponse
    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateUserRequest request
    ) {
        // 1. Llamamos al servicio (que nos devuelve la Entidad actualizada)
        User updatedUser = userService.updateMyProfile(userDetails.getUsername(), request);

        // 2. Convertimos la Entidad a DTO para evitar ciclos y ocultar datos sensibles
        UserResponse response = new UserResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole()
        );

        return ResponseEntity.ok(response);
    }
}