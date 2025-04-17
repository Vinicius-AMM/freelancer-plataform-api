package com.manager.freelancer_management_api.domain.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(description = "DTO que representa mensagem de erro")
public record ApiResponseDTO(int statusCode,
                             String message,
                             LocalDateTime timestamp) {
    public ApiResponseDTO(HttpStatus status, String message) {
        this(status.value(), message, LocalDateTime.now());
    }
}