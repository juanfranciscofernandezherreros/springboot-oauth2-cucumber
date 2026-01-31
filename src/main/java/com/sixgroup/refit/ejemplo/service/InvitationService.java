package com.sixgroup.refit.ejemplo.service;

import com.sixgroup.refit.ejemplo.dto.InvitationCreatedEvent;
import com.sixgroup.refit.ejemplo.model.Invitation;
import com.sixgroup.refit.ejemplo.model.InvitationStatus;
import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.repository.InvitationRepository;
import com.sixgroup.refit.ejemplo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public Invitation create(String email, Role role) {

        if (repository.existsByEmailAndStatus(email, InvitationStatus.PENDING)) {
            throw new IllegalStateException("Ya existe una invitación pendiente");
        }

        Invitation invitation = Invitation.builder()
                .email(email)
                .role(role)
                .token(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(48, ChronoUnit.HOURS))
                .build();

        repository.save(invitation);

        rabbitTemplate.convertAndSend(
                "invitation.exchange",
                "invitation.created",
                new InvitationCreatedEvent(
                        invitation.getEmail(),
                        invitation.getToken(),
                        invitation.getExpiresAt()
                )
        );

        return invitation;
    }

    public Invitation validate(String token) {
        return repository.findByTokenAndStatus(token, InvitationStatus.PENDING)
                .filter(inv -> inv.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public void accept(String token, String password, UserRepository userRepo, PasswordEncoder encoder) {

        Invitation inv = validate(token);

        userRepo.save(
                User.builder()
                        .email(inv.getEmail())
                        .password(encoder.encode(password))
                        .role(inv.getRole())
                        .accountNonLocked(true)
                        .build()
        );

        inv.setStatus(InvitationStatus.ACCEPTED);
        repository.save(inv);
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // sin timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        return emitter;
    }

    public void publish(Invitation invitation) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("invitation-created")
                                .data(invitation)
                );
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }
}
