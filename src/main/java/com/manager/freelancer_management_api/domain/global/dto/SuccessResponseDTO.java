package com.manager.freelancer_management_api.domain.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(description = "DTO que representa mensagem de sucesso")
public record SuccessResponseDTO(int statusCode,
                                 String message,
                                 LocalDateTime timestamp) {
    public SuccessResponseDTO(String message) {
        this(HttpStatus.OK.value(), message, LocalDateTime.now());
    }
}
