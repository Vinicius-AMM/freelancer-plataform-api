package com.manager.freelancer_management_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO para autenticação do usuário")
public record LoginRequestDTO(
        @NotNull(message = "Fill in this field")
        @Email(message = "Invalid email format")
        @Schema(example = "test@test.com")
        String email,
        @NotNull(message = "Fill in this field")
        @Schema(example = "password")
        String password
) {
}
