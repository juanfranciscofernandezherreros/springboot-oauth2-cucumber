package com.sixgroup.refit.ejemplo.dto;

import com.sixgroup.refit.ejemplo.model.Role;

public record CreateInvitationRequest(
        String email,
        Role role
) {}

