package com.manager.freelancer_management_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para atualização do papel do usuário.")
public record UserRoleUpdateRequestDTO(@Schema(example = "FREELANCER") String newUserRole) {
}