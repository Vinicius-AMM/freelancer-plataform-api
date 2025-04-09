package com.manager.freelancer_management_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para atualização do documento do usuário")
public record DocumentUpdateRequestDTO(@Schema(example = "password")
                                       String password,
                                       @Schema(example = "10987654321")
                                       @NotBlank(message = "New document cannot be empty.")
                                       String newDocument) {
}
