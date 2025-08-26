package com.manager.freelancer_management_api.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ApproveProjectRequestDTO(
        @Schema(example = "password")
        @NotBlank(message = "Password must not be empty.")
        String rawPassword) {
}