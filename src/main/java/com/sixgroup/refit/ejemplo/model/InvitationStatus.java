package com.sixgroup.refit.ejemplo.model;

import java.util.List;

public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    APPROVED;

    public boolean canTransitionTo(InvitationStatus nextStatus) {
        return switch (this) {
            case PENDING -> List.of(ACCEPTED, REJECTED, EXPIRED).contains(nextStatus);

            // Una vez aceptada, puede aprobarse definitivamente o rechazarse tras revisión
            case ACCEPTED -> List.of(APPROVED, REJECTED).contains(nextStatus);

            // Si está expirada, quizás permitas re-activarla (volver a PENDING) o rechazarla
            case EXPIRED -> List.of(PENDING, REJECTED).contains(nextStatus);

            // APPROVED y REJECTED suelen ser estados finales (nodos hoja)
            case APPROVED, REJECTED -> false;

            default -> false;
        };
    }
}
