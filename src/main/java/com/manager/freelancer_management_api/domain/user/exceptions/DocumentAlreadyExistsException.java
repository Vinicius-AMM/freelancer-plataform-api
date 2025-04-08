package com.manager.freelancer_management_api.domain.user.exceptions;

import com.manager.freelancer_management_api.domain.exceptions.BusinessException;

public class DocumentAlreadyExistsException extends BusinessException {
    public DocumentAlreadyExistsException() {
        super("Document already exists.");
    }
}