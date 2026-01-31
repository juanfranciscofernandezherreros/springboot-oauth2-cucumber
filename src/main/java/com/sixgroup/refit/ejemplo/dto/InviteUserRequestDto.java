package com.sixgroup.refit.ejemplo.dto;

import com.sixgroup.refit.ejemplo.model.Role;

public record InviteUserRequestDto(
        String name,
        String email,
        Role role
) {}

