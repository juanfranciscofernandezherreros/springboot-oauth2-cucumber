package com.fernandez.backend.dto;

import com.fernandez.backend.model.Role;

public record AdminUpdateUserRequest(
        String name,
        Role role,
        Boolean accountNonLocked
) {
}

