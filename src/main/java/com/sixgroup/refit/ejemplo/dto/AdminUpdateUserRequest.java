package com.sixgroup.refit.ejemplo.dto;

import com.sixgroup.refit.ejemplo.model.Role;

public record AdminUpdateUserRequest(
        String name,
        Role role,
        Boolean accountNonLocked
) {
}

