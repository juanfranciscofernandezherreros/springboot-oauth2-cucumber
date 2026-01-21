package com.sixgroup.refit.ejemplo.exceptions;

public class InvalidRoleForRegistrationException extends RuntimeException {

    public InvalidRoleForRegistrationException() {
        super("Solo se permite registrar usuarios con rol USER");
    }
}

