package com.manager.freelancer_management_api.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO que retorna o token de autenticação")
public record LoginResponseDTO(String token) {
}