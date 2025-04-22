package com.manager.freelancer_management_api.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DeleteProjectRequestDTO (@Schema(description = "Senha do usuário proprietário do projeto", example = "password", nullable = false)
                                       @NotBlank(message = "Password must not be empty.")
                                       String rawPassword) {
}
