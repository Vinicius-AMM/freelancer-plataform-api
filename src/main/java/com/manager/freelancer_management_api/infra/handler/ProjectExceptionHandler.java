package com.manager.freelancer_management_api.infra.handler;

import com.manager.freelancer_management_api.domain.global.dto.ApiResponseDTO;
import com.manager.freelancer_management_api.domain.project.exceptions.ProjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.manager.freelancer_management_api.utils.handler.ApiResponseUtil.buildErrorResponse;

@ControllerAdvice
public class ProjectExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiResponseDTO> projectNotFoundHandler(ProjectNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

}
