package com.manager.freelancer_management_api.domain.user.exceptions;

import com.manager.freelancer_management_api.domain.exceptions.BusinessException;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException() {
        super("Email already exists.");
    }
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}