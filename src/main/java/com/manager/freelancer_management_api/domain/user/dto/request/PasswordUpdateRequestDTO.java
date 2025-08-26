package com.manager.freelancer_management_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para solicitação de atualização de senha.")
public record PasswordUpdateRequestDTO(@Schema(example = "password")
                                       @NotBlank(message = "Password cannot be empty.")
                                       String oldPassword,
                                       @Schema(example = "newpassword")
                                       @NotBlank(message = "Password cannot be empty.")
                                       @Size(min = 6, max = 100, message = "The password must have at least 6 characters.")
                                       String newPassword) {
}
