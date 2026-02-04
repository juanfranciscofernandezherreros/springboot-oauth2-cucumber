package com.fernandez.backend.dto;

import com.fernandez.backend.model.Role;

public record RegisterRequest(
        String name,
        String email,
        String password,
        Role role
) {
}