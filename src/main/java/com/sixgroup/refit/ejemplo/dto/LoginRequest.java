package com.sixgroup.refit.ejemplo.dto;

public record LoginRequest(
        String email,
        String password
) {
}