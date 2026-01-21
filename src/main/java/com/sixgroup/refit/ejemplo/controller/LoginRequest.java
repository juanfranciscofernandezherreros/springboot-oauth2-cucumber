package com.sixgroup.refit.ejemplo.controller;

public record LoginRequest(
        String email,
        String password
) {
}