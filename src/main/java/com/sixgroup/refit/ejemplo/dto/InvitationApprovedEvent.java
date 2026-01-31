package com.sixgroup.refit.ejemplo.dto;

public record InvitationApprovedEvent(
        Long invitationId,
        String email,
        String token
) {}
