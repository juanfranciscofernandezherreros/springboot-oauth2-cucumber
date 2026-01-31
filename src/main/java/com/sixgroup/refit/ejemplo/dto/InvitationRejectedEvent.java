package com.sixgroup.refit.ejemplo.dto;

public record InvitationRejectedEvent(
        Long invitationId,
        String email,
        String reason
) {}

