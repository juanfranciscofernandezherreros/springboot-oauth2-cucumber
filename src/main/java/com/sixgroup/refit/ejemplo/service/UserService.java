package com.sixgroup.refit.ejemplo.service;

import com.sixgroup.refit.ejemplo.dto.AdminUpdateUserRequest;
import com.sixgroup.refit.ejemplo.dto.AdminUserListResponse;
import com.sixgroup.refit.ejemplo.dto.UpdateUserRequest;
import com.sixgroup.refit.ejemplo.dto.UserStatsResponse;
import com.sixgroup.refit.ejemplo.model.InvitationStatus;
import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.repository.InvitationRepository;
import com.sixgroup.refit.ejemplo.repository.UserRepository;
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
    private final InvitationRepository invitationRepository;

    // =====================================================
    // ESTADÍSTICAS (Métricas del Panel)
    // =====================================================
    public UserStatsResponse getUserStatistics() {
        long total = userRepository.count();
        long blocked = userRepository.countByAccountNonLockedFalse();
        long pendingInvs = invitationRepository.countByStatus(InvitationStatus.PENDING);

        log.info("MÉTRICAS: Total:{}, Bloqueados:{}, Invitaciones Pendientes:{}", total, blocked, pendingInvs);

        // Uso directo de constructor con 'new'
        return new UserStatsResponse(total, blocked, pendingInvs);
    }

    // =====================================================
    // GESTIÓN DE LISTADOS (ADMIN)
    // =====================================================
    public List<AdminUserListResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toAdminUserListResponse)
                .toList();
    }

    public List<User> getLockedUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isAccountNonLocked())
                .toList();
    }

    private AdminUserListResponse toAdminUserListResponse(User user) {
        // Aquí uso el builder porque AdminUserListResponse tiene muchos campos,
        // pero podrías usar 'new' si tienes el constructor adecuado.
        return AdminUserListResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .accountNonLocked(user.isAccountNonLocked())
                .failedAttempt(user.getFailedAttempt())
                .build();
    }

    // =====================================================
    // ACCIONES DE BLOQUEO / DESBLOQUEO
    // =====================================================
    @Transactional
    public void unlockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockCount(0);
        user.setLockTime(null);

        userRepository.save(user);
        log.info("ADMIN: Usuario {} ha sido desbloqueado.", email);
    }

    @Transactional
    public void lockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        user.setAccountNonLocked(false);
        userRepository.save(user);
        log.info("ADMIN: Usuario {} ha sido bloqueado.", email);
    }

    // =====================================================
    // ACTUALIZACIÓN Y BORRADO (ADMIN)
    // =====================================================
    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("No está permitido eliminar a otros administradores.");
        }

        userRepository.delete(user);
        log.info("ADMIN: Usuario con id {} eliminado.", id);
    }

    @Transactional
    public void updateUserRole(String email, Role newRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            throw new RuntimeException("No puedes degradar a otro ADMIN.");
        }

        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional
    public AdminUserListResponse updateUserByAdmin(Long id, AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("No está permitido modificar a otros administradores.");
        }

        if (request.name() != null) user.setName(request.name());
        if (request.role() != null) user.setRole(request.role());
        if (request.accountNonLocked() != null) user.setAccountNonLocked(request.accountNonLocked());

        User updatedUser = userRepository.save(user);
        return toAdminUserListResponse(updatedUser);
    }

    // =====================================================
    // PERFIL DE USUARIO (SELF)
    // =====================================================
    public User getUserStatus(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Transactional
    public User updateMyProfile(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (request.name() != null) {
            user.setName(request.name());
        }

        return userRepository.save(user);
    }
}