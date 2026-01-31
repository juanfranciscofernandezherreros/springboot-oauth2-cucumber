package com.sixgroup.refit.ejemplo.controller;

import com.sixgroup.refit.ejemplo.config.InvitationRabbitConfig;
import com.sixgroup.refit.ejemplo.dto.CreateInvitationRequest;
import com.sixgroup.refit.ejemplo.dto.InvitationCreatedEvent;
import com.sixgroup.refit.ejemplo.model.Invitation;
import com.sixgroup.refit.ejemplo.model.InvitationStatus;
import com.sixgroup.refit.ejemplo.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RabbitTemplate rabbitTemplate;

    @PostMapping
    @Transactional // 🔥 Si falla el envío a Rabbit, se hace rollback de la DB
    public ResponseEntity<Void> createInvitation(@RequestBody CreateInvitationRequest request) {

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

        invitationRepository.save(invitation);

        // 3. Publicar evento (Usando las constantes correctas de la Config)
        InvitationCreatedEvent event = new InvitationCreatedEvent(
                invitation.getEmail(),
                invitation.getToken(),
                invitation.getExpiresAt()
        );

        rabbitTemplate.convertAndSend(
                InvitationRabbitConfig.EXCHANGE,
                InvitationRabbitConfig.ROUTING_KEY,
                event
        );

        return ResponseEntity.accepted().build();
    }
}