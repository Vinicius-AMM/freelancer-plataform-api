package com.manager.freelancer_management_api.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ProjectCompletionRequestDTO(@Schema(description = "Senha do freelancer associado projeto", example = "password", nullable = false)
                                          @NotBlank(message = "Password must not be empty.")
                                          String rawPassword,

                                          @Schema(description = "Notas do freelancer sobre a entrega do projeto", example = "Documentação do projeto.")
                                          @NotBlank(message = "Notes cannot be empty.")
                                          String notes
) {
}
