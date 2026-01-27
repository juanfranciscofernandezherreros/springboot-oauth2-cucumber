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

import static com.sixgroup.refit.ejemplo.utils.AdminApiPaths.*;

@RestController
@RequestMapping(BASE)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AuthService authService;

    // =====================================================
    // CREATE
    // =====================================================
    @PostMapping(CREATE_USER)
    public ResponseEntity<Void> createUserFromPanel(
            @RequestBody AdminCreateUserRequest request
    ) {
        authService.registerByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // =====================================================
    // READ
    // =====================================================
    @GetMapping(USERS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<AdminUserListResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(LOCKED_USERS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<User>> getLockedUsers() {
        return ResponseEntity.ok(userService.getLockedUsers());
    }

    @GetMapping(USER_STATUS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<User> getUserStatus(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserStatus(email));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @PostMapping(UNLOCK_USER)
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<Map<String, String>> unlockUser(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        userService.unlockUser(email);
        return ResponseEntity.ok(
                Map.of("mensaje", "Usuario " + email + " ha sido habilitado de nuevo.")
        );
    }

    @PutMapping(UPDATE_ROLE)
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<Map<String, String>> updateRole(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        Role role = Role.valueOf(request.get("role").toUpperCase());
        userService.updateUserRole(email, role);

        return ResponseEntity.ok(
                Map.of("mensaje", "Rol de " + email + " actualizado a " + role)
        );
    }

    // =====================================================
    // DELETE
    // =====================================================
    @DeleteMapping(DELETE_USER)
    @PreAuthorize("hasAuthority('admin:delete')")
    public ResponseEntity<Map<String, String>> deleteUser(
            @RequestParam String email
    ) {
        userService.deleteUser(email);
        return ResponseEntity.ok(
                Map.of("mensaje", "Usuario " + email + " eliminado correctamente.")
        );
    }
}
