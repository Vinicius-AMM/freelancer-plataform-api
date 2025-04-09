package com.manager.freelancer_management_api.domain.exceptions;

public class UnauthorizedAccessException extends BusinessException {
    public UnauthorizedAccessException() {
        super("Access denied.");
    }
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}