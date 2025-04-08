package com.manager.freelancer_management_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para o deletar um usuário")
public record DeleteUserRequestDTO(
        @Schema(example = "password") String password) {
}
