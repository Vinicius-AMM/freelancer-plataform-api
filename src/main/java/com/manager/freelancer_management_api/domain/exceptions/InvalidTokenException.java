package com.manager.freelancer_management_api.domain.exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(){
        super("Invalid Token.");
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}