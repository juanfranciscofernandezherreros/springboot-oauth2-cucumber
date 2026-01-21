package com.sixgroup.refit.ejemplo.controller;

import com.sixgroup.refit.ejemplo.model.Role;

public record AdminCreateUserRequest(
        String name,
        String email,
        String password,
        Role role // <--- Este sí permite elegir
) {}
