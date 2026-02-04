package com.fernandez.backend.dto;

import com.fernandez.backend.model.Role;

public record AdminCreateUserRequest(
        String name,
        String email,
        String password,
        Role role // <--- Este sí permite elegir
) {}
