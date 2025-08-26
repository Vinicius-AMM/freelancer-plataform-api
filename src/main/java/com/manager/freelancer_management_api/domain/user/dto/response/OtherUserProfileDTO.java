package com.manager.freelancer_management_api.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "DTO contendo o perfil limitado de outro usuário")
public record OtherUserProfileDTO(String fullName,
                                  String mainUserRole,
                                  String currentUserRole) implements Serializable, UserProfileResponseDTO {
}
