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
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    // Para la pestaña de "Pendientes"
    List<Invitation> findByStatusOrderByCreatedAtDesc(InvitationStatus status);

    // Para la pestaña de "Histórico" (Cualquier estado que no sea PENDING)
    List<Invitation> findByStatusNotOrderByCreatedAtDesc(InvitationStatus status);

    // Para el contador (2)
    long countByStatus(InvitationStatus status);

    boolean existsByEmailAndStatus(String email, InvitationStatus status);
}