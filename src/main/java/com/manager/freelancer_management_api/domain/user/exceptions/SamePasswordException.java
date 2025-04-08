package com.manager.freelancer_management_api.domain.user.exceptions;

import com.manager.freelancer_management_api.domain.exceptions.BusinessException;

public class SamePasswordException extends BusinessException {
    public SamePasswordException() {
        super("Passwords must not be the same.");
    }
}