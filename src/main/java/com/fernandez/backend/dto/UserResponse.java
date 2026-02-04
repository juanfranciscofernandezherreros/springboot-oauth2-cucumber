package com.fernandez.backend.dto;

import com.fernandez.backend.model.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role
) {}
