package com.sixgroup.refit.ejemplo.controller;

import com.sixgroup.refit.ejemplo.dto.CreateInvitationRequest;
import com.sixgroup.refit.ejemplo.model.Invitation;
import com.sixgroup.refit.ejemplo.model.InvitationStatus;
import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/invitations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InvitationController {

    private final InvitationRepository invitationRepository;

    // =====================================================
    // GESTIÓN DE LISTADOS Y FILTROS
    // =====================================================

    /**
     * Endpoint Maestro: Lista todo o filtra por estados específicos.
     * Uso: /all?statuses=PENDING,ACCEPTED
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<Invitation>> getAll(
            @RequestParam(required = false) List<InvitationStatus> statuses) {

        if (statuses == null || statuses.isEmpty()) {
            return ResponseEntity.ok(invitationRepository.findAllByOrderByCreatedAtDesc());
        }
        return ResponseEntity.ok(invitationRepository.findByStatusInOrderByCreatedAtDesc(statuses));
    }

    /**
     * Retorna todos los tipos de estado disponibles en el sistema.
     */
    @GetMapping("/statuses")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<InvitationStatus>> getAvailableStatuses() {
        return ResponseEntity.ok(Arrays.asList(InvitationStatus.values()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<Invitation>> getPendingInvitations() {
        return ResponseEntity.ok(invitationRepository.findByStatusOrderByCreatedAtDesc(InvitationStatus.PENDING));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<Invitation>> getHistory() {
        return ResponseEntity.ok(invitationRepository.findByStatusNotOrderByCreatedAtDesc(InvitationStatus.PENDING));
    }

    // =====================================================
    // ACCIONES (CREACIÓN Y ESTADOS)
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('admin:create')")
    @Transactional
    public ResponseEntity<?> createInvitation(@RequestBody CreateInvitationRequest request) {
        if (invitationRepository.existsByEmailAndStatus(request.email(), InvitationStatus.PENDING)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe una invitación pendiente para este correo.");
        }

        Invitation invitation = Invitation.builder()
                .email(request.email())
                .name(request.name())
                .description(request.description())
                .role(Role.USER)
                .token(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(48, ChronoUnit.HOURS))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(invitationRepository.save(invitation));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('admin:update')")
    @Transactional
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam InvitationStatus newStatus) {

        return invitationRepository.findById(id)
                .map(invitation -> {
                    if (!invitation.getStatus().canTransitionTo(newStatus)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Transición no permitida: de " + invitation.getStatus() + " a " + newStatus);
                    }

                    invitation.setStatus(newStatus);
                    invitationRepository.save(invitation);
                    return ResponseEntity.ok("Estado actualizado a " + newStatus);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}