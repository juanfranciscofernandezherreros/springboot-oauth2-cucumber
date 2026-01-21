package com.sixgroup.refit.ejemplo.admin;

import com.sixgroup.refit.ejemplo.controller.AdminCreateUserRequest;
import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.service.AuthService;
import com.sixgroup.refit.ejemplo.usuario.User;
import com.sixgroup.refit.ejemplo.usuario.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AuthService service; // O UserService si moviste el método allí

    @PostMapping("/create-user")
    public ResponseEntity<Void> createUserFromPanel(@RequestBody AdminCreateUserRequest request) {
        service.registerByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Obtener la lista de todos los usuarios del sistema.
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Obtener solo los usuarios que están actualmente bloqueados.
     */
    @GetMapping("/locked-users")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<User>> getLockedUsers() {
        return ResponseEntity.ok(userService.getLockedUsers());
    }

    /**
     * Ver el estado detallado de un usuario específico por su email.
     */
    @GetMapping("/user-status")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<User> getUserStatus(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserStatus(email));
    }

    /**
     * Desbloquear a un usuario (resetea contadores de fallos y desbloquea cuenta).
     */
    @PostMapping("/unlock")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<?> unlockUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.unlockUser(email);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario " + email + " ha sido habilitado de nuevo."));
    }

    /**
     * Actualizar el rol de un usuario.
     * Nota: UserService impedirá degradar a otros ADMINs.
     */
    @PutMapping("/update-role")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<?> updateRole(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Role role = Role.valueOf(request.get("role").toUpperCase());
        userService.updateUserRole(email, role);
        return ResponseEntity.ok(Map.of("mensaje", "Rol de " + email + " actualizado a " + role));
    }

    /**
     * Eliminar un usuario permanentemente.
     * Nota: UserService impedirá borrar a otros ADMINs.
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('admin:delete')")
    public ResponseEntity<?> deleteUser(@RequestParam String email) {
        userService.deleteUser(email);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario " + email + " eliminado correctamente."));
    }
}