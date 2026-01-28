package com.sixgroup.refit.ejemplo.service;

import com.sixgroup.refit.ejemplo.dto.AdminUserListResponse;
import com.sixgroup.refit.ejemplo.dto.UpdateUserRequest;
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
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        // PROTECCIÓN: Un Admin no borra a otro Admin
        if (user.getRole() == Role.ADMIN) {
            log.error("SEGURIDAD: Intento denegado de borrar al administrador {}", email);
            throw new RuntimeException("No está permitido eliminar a otros administradores.");
        }

        userRepository.delete(user);
        log.info("ADMIN: Usuario {} eliminado.", email);
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
}