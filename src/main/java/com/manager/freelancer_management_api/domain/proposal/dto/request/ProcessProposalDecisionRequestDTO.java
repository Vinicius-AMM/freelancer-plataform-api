package com.manager.freelancer_management_api.domain.proposal.dto.request;

import com.manager.freelancer_management_api.domain.proposal.enums.ProposalDecisionAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProcessProposalDecisionRequestDTO(@NotNull(message = "Action cannot be null.")
                                                @Schema(description = "Ação a ser tomada na proposta (ACCEPT ou REJECT)", example = "ACCEPT")
                                                ProposalDecisionAction action,
                                                @NotBlank(message = "Reason cannot be empty.")
                                                @Schema(example = "password")
                                                String rawPassword
) {
}