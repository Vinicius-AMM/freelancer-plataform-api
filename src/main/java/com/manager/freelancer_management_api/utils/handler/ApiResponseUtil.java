package com.manager.freelancer_management_api.utils.handler;

import com.manager.freelancer_management_api.domain.dto.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ApiResponseUtil {

    private ApiResponseUtil() {
    }

    public static ResponseEntity<ApiResponseDTO> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiResponseDTO(status, message));
    }

    public static ResponseEntity<ApiResponseDTO> buildSuccessResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiResponseDTO(status, message));
    }
}