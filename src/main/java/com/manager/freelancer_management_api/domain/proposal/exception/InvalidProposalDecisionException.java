package com.manager.freelancer_management_api.domain.proposal.exception;

import com.manager.freelancer_management_api.domain.global.exceptions.BusinessException;

public class InvalidProposalDecisionException extends BusinessException {
    public InvalidProposalDecisionException(String message) {
        super(message);
    }
}
