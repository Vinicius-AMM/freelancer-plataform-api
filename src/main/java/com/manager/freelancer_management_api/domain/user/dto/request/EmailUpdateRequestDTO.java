package com.manager.freelancer_management_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para atualização do email do usuário")
public record EmailUpdateRequestDTO(@Schema(example = "password") String password,
                                    @Schema(example = "newemail@test.com") String email) {
}
