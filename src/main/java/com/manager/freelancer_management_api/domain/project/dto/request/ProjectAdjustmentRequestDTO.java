package com.manager.freelancer_management_api.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ProjectAdjustmentRequestDTO(@Schema(description = "Senha do cliente dono do projeto", example = "password", nullable = false)
                                          @NotBlank(message = "Password must not be empty.")
                                          String rawPassword,

                                          @Schema(description = "Detalhes dos ajustes solicitados pelo cliente", example = "A funcionalidade 1 precisa de melhorias.")
                                          @NotBlank(message = "Adjustment details cannot be blank.")
                                          String adjustmentDetails
) {
}
