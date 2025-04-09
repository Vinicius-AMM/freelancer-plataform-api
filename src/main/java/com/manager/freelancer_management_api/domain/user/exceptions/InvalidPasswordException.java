package com.manager.freelancer_management_api.domain.user.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class InvalidPasswordException extends BadCredentialsException {
    public InvalidPasswordException() {
        super("Invalid password.");
    }
    public InvalidPasswordException(String message) {
        super(message);
    }
}