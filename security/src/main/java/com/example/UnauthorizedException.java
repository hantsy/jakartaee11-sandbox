package com.example;

import jakarta.security.enterprise.AuthenticationException;

public class UnauthorizedException extends AuthenticationException {
    public UnauthorizedException() {
        super("401 Unauthorized");
    }
}
