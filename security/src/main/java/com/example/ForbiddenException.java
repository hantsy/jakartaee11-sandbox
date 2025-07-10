package com.example;

import jakarta.security.enterprise.AuthenticationException;

public class ForbiddenException extends AuthenticationException {
    public ForbiddenException() {
        super("Forbidden");
    }
}
