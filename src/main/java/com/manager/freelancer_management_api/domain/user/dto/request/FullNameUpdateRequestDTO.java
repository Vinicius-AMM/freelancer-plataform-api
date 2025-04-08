package com.manager.freelancer_management_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para atualização do nome completo do usuário")
public record FullNameUpdateRequestDTO(@Schema(example = "New Full Name")
                                       @NotBlank String newFullName) {
}