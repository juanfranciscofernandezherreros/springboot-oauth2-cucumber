package com.sixgroup.refit.ejemplo.controller;

import com.sixgroup.refit.ejemplo.dto.CreateInvitationRequest;
import com.sixgroup.refit.ejemplo.model.Invitation;
import com.sixgroup.refit.ejemplo.model.InvitationStatus;
import com.sixgroup.refit.ejemplo.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationRepository invitationRepository;

    @PostMapping
    @Transactional // 🔥 Si falla el envío a Rabbit, se hace rollback de la DB
    public ResponseEntity<Invitation> createInvitation(@RequestBody CreateInvitationRequest request) {

        // 1. Validar duplicados
        if (invitationRepository.existsByEmailAndStatus(request.email(), InvitationStatus.PENDING)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // 2. Persistir en base de datos
        Invitation invitation = Invitation.builder()
                .email(request.email())
                .role(request.role())
                .token(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(48, ChronoUnit.HOURS))
                .build();

        return ResponseEntity.ok(invitationRepository.save(invitation));
    }
}