package com.manager.freelancer_management_api.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "DTO contendo o perfil do usuário autenticado")
public record UserProfileDTO(String fullName,
                             String email,
                             String document,
                             String mainUserRole,
                             String currentUserRole) implements Serializable, UserProfileResponseDTO {
}