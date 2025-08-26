package com.manager.freelancer_management_api.domain.user.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class InvalidEmailException extends BadCredentialsException {
    public InvalidEmailException(String message) {
        super(message);
    }
}