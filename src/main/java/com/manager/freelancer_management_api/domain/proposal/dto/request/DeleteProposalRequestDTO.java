package com.manager.freelancer_management_api.domain.proposal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DeleteProposalRequestDTO(@Schema(description = "Senha do usuário proprietário da proposta", example = "password", nullable = false)
                                       @NotBlank(message = "Password must not be empty.")
                                       String rawPassword) {
}