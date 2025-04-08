package com.manager.freelancer_management_api.domain.exceptions;

public class PrivateKeyLoadException extends RuntimeException {
    public PrivateKeyLoadException(String message) {
        super(message);
    }
}