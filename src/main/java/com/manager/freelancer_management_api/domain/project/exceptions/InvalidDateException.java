package com.manager.freelancer_management_api.domain.project.exceptions;

import com.manager.freelancer_management_api.domain.global.exceptions.BusinessException;

public class InvalidDateException extends BusinessException {
    public InvalidDateException(String message) {
        super(message);
    }
}
