package com.manager.freelancer_management_api.domain.global.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

public record DTOValidationErrorResponse(int statusCode,
                                         Map<String, String> errors,
                                         LocalDateTime timestamp) {
    public DTOValidationErrorResponse(HttpStatus status, Map<String, String> errors) {
        this(status.value(), errors, LocalDateTime.now());
    }
}