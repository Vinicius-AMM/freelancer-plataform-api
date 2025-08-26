package com.manager.freelancer_management_api.infra.handler;

import com.manager.freelancer_management_api.domain.global.dto.ApiResponseDTO;
import com.manager.freelancer_management_api.domain.global.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.manager.freelancer_management_api.utils.handler.ApiResponseUtil.buildErrorResponse;

@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ApiResponseDTO> invalidDateHandler(InvalidDateException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponseDTO> invalidTokenHandler(InvalidTokenException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(PublicKeyLoadException.class)
    public ResponseEntity<ApiResponseDTO> publicKeyLoadHandler(PublicKeyLoadException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(PrivateKeyLoadException.class)
    public ResponseEntity<ApiResponseDTO> privateKeyLoadHandler(PrivateKeyLoadException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponseDTO> unauthorizedAccessHandler(UnauthorizedAccessException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
}