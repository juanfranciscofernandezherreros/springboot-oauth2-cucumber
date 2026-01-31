package com.sixgroup.refit.ejemplo.repository;

import com.sixgroup.refit.ejemplo.model.Invitation;
import com.sixgroup.refit.ejemplo.model.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    // =====================================================
    // ADMIN / CREACIÓN
    // =====================================================

    boolean existsByEmailAndStatus(String email, InvitationStatus status);

    // =====================================================
    // ADMIN PANEL
    // =====================================================

    Optional<Invitation> findByToken(String token);

    List<Invitation> findAllByStatus(InvitationStatus status);

    // =====================================================
    // USO PÚBLICO / VALIDACIONES
    // =====================================================

    Optional<Invitation> findByTokenAndStatus(
            String token,
            InvitationStatus status
    );

    // =====================================================
    // EXPIRACIÓN AUTOMÁTICA
    // =====================================================

    List<Invitation> findAllByStatusAndExpiresAtBefore(
            InvitationStatus status,
            Instant now
    );
}
