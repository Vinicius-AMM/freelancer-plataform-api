package com.manager.freelancer_management_api.domain.user.exceptions;

import com.manager.freelancer_management_api.domain.exceptions.BusinessException;

public class InvalidUserRoleException extends BusinessException {
    public InvalidUserRoleException() {
        super("Invalid role. Valid roles are CLIENT or FREELANCER.");
    }
}