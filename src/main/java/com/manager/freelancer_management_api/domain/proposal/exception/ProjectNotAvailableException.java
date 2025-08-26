package com.manager.freelancer_management_api.domain.proposal.exception;

import com.manager.freelancer_management_api.domain.global.exceptions.BusinessException;

public class ProjectNotAvailableException extends BusinessException {
    public ProjectNotAvailableException(String message) {
        super(message);
    }
}
