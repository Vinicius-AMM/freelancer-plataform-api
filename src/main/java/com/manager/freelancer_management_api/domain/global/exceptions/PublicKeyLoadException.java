package com.manager.freelancer_management_api.domain.global.exceptions;

public class PublicKeyLoadException extends RuntimeException {
    public PublicKeyLoadException(String message) {
        super(message);
    }
}