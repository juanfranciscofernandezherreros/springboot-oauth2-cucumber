package com.sixgroup.refit.ejemplo.service;

import com.sixgroup.refit.ejemplo.dto.AdminUpdateUserRequest;
import com.sixgroup.refit.ejemplo.dto.AdminUserListResponse;
import com.sixgroup.refit.ejemplo.dto.UpdateUserRequest;
import com.sixgroup.refit.ejemplo.dto.UserStatsResponse;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.repository.UserRepository;
import com.sixgroup.refit.ejemplo.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<AdminUserListResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toAdminUserListResponse)
                .toList();
    }


    private AdminUserListResponse toAdminUserListResponse(User user) {
        return AdminUserListResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .accountNonLocked(user.isAccountNonLocked())
                .failedAttempt(user.getFailedAttempt())
                .build();
    }

    public List<User> getLockedUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isAccountNonLocked())
                .toList();
    }

    @Transactional
    public void unlockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockCount(0);
        user.setLockTime(null);

        userRepository.save(user);
        log.info("ADMIN: Usuario {} ha sido desbloqueado manualmente.", email);
    }

    @Transactional
    public void lockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setAccountNonLocked(false);
        userRepository.save(user);
    }


    @Transactional
    public void deleteUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        // PROTECCIÓN: un Admin no puede borrar a otro Admin
        if (user.getRole() == Role.ADMIN) {
            log.error("SEGURIDAD: Intento denegado de borrar al administrador con id {}", id);
            throw new RuntimeException("No está permitido eliminar a otros administradores.");
        }

        userRepository.delete(user);
        log.info("ADMIN: Usuario con id {} eliminado.", id);
    }


    @Transactional
    public void updateUserRole(String email, Role newRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        // PROTECCIÓN: No degradar a otros Admins
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            log.error("SEGURIDAD: Intento de degradar al administrador {}", email);
            throw new RuntimeException("No puedes quitarle privilegios de administrador a otro ADMIN.");
        }

        user.setRole(newRole);
        userRepository.save(user);
    }

    // --- MÉTODOS DE USUARIO (ROLE_USER) ---

    public User getUserStatus(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Actualiza el perfil propio.
     * Blindado: Solo cambia el nombre, ignorando roles y estados de bloqueo.
     */
    @Transactional
    public User updateMyProfile(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (request.name() != null) {
            user.setName(request.name());
        }

        log.info("USER: Perfil de {} actualizado.", email);
        return userRepository.save(user);
    }

    @Transactional
    public AdminUserListResponse updateUserByAdmin(Long id, AdminUpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        // PROTECCIÓN: no modificar a otros administradores
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("No está permitido modificar a otros administradores.");
        }

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.role() != null) {
            user.setRole(request.role());
        }

        if (request.accountNonLocked() != null) {
            user.setAccountNonLocked(request.accountNonLocked());
        }

        User updatedUser = userRepository.save(user);

        log.info("ADMIN: Usuario con id {} actualizado.", id);

        return AdminUserListResponse.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole())
                .accountNonLocked(updatedUser.isAccountNonLocked())
                .failedAttempt(updatedUser.getFailedAttempt())
                .build();
    }

    public UserStatsResponse getUserStatistics() {
        long total = userRepository.count();
        long blocked = userRepository.countByAccountNonLockedFalse();
        long pending = userRepository.countByPasswordIsNull();
        return new UserStatsResponse(total, blocked, pending);
    }


}
