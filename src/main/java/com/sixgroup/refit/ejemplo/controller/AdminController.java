package com.sixgroup.refit.ejemplo.controller;

import com.sixgroup.refit.ejemplo.dto.AdminCreateUserRequest;
import com.sixgroup.refit.ejemplo.dto.AdminUpdateUserRequest;
import com.sixgroup.refit.ejemplo.dto.AdminUserListResponse;
import com.sixgroup.refit.ejemplo.dto.UserStatsResponse;
import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.service.AuthService;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.sixgroup.refit.ejemplo.utils.constants.AdminApiPaths.*;

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
    @PostMapping(UNLOCK_USER + "/{email}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<Map<String, String>> unLockUser(
            @PathVariable String email
    ) {
        userService.unlockUser(email);

        return ResponseEntity.ok(
                Map.of("mensaje", "Usuario " + email + " ha sido desbloqueado correctamente.")
        );
    }


    // =====================================================
    // UPDATE
    // =====================================================
    @PostMapping(LOCK_USER + "/{email}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<Map<String, String>> lockUser(
            @PathVariable String email
    ) {
        userService.lockUser(email);

        return ResponseEntity.ok(
                Map.of("mensaje", "Usuario " + email + " ha sido bloqueado correctamente.")
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
    // =====================================================
// DELETE
// =====================================================
    @DeleteMapping(DELETE_USER + "/{id}")
    @PreAuthorize("hasAuthority('admin:delete')")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id) {

        userService.deleteUserById(id);

        return ResponseEntity.ok(
                Map.of("mensaje", "Usuario con id " + id + " eliminado correctamente.")
        );
    }


    @PutMapping(UPDATE_USER + "/{id}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<AdminUserListResponse> updateUser(
            @PathVariable Long id,
            @RequestBody AdminUpdateUserRequest request
    ) {
        AdminUserListResponse response = userService.updateUserByAdmin(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(STATS)
    public ResponseEntity<UserStatsResponse> getUserStats() {
        return ResponseEntity.ok(userService.getUserStatistics());
    }


}
