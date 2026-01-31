package com.sixgroup.refit.ejemplo.consumer;

import com.sixgroup.refit.ejemplo.config.InvitationRabbitConfig;
import com.sixgroup.refit.ejemplo.dto.InvitationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationConsumer {

    // Herramienta de Spring para enviar mensajes a través de WebSockets
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Este método se dispara automáticamente cuando RabbitMQ recibe un mensaje
     * en la cola 'invitation.created.queue'.
     */
    @RabbitListener(queues = InvitationRabbitConfig.QUEUE)
    public void handleInvitationCreated(InvitationCreatedEvent event) {
        // 1. Log de auditoría para ver qué está pasando en la consola
        log.info("📩 Mensaje recibido desde RabbitMQ:");
        log.info("   ↳ Email: {}", event.email());
        log.info("   ↳ Token: {}", event.token());
        log.info("   ↳ Expira: {}", event.expiresAt());

        try {
            // 2. Envío al Dashboard en tiempo real vía WebSocket
            // Esto es lo que hará que la fila aparezca "mágicamente" en tu tabla HTML
            messagingTemplate.convertAndSend("/topic/invitations", event);

            log.info("🚀 [WebSocket] Evento retransmitido al dashboard con éxito.");
        } catch (Exception e) {
            log.error("❌ Fallo al notificar al dashboard: {}", e.getMessage());
        }
    }
}