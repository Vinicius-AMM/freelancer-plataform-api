package com.manager.freelancer_management_api.infra.handler;

import com.manager.freelancer_management_api.domain.global.dto.ApiResponseDTO;
import com.manager.freelancer_management_api.domain.proposal.exception.InvalidProposalDecisionException;
import com.manager.freelancer_management_api.domain.proposal.exception.ProjectNotAvailableException;
import com.manager.freelancer_management_api.domain.proposal.exception.ProposalNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.manager.freelancer_management_api.utils.handler.ApiResponseUtil.buildErrorResponse;

@ControllerAdvice
public class ProposalExceptionHandler {

    @ExceptionHandler(ProposalNotFoundException.class)
    public ResponseEntity<ApiResponseDTO> proposalNotFoundHandler(ProposalNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidProposalDecisionException.class)
    public ResponseEntity<ApiResponseDTO> invalidProposalDecisionHandler(InvalidProposalDecisionException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ProjectNotAvailableException.class)
    public ResponseEntity<ApiResponseDTO> projectNotAvailableHandler(ProjectNotAvailableException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
}