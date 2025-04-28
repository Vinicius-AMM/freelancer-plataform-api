package com.manager.freelancer_management_api.domain.proposal.dto.response;

import com.manager.freelancer_management_api.domain.global.dto.DeadlineResponseDTO;
import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import com.manager.freelancer_management_api.domain.user.dto.response.OtherUserProfileDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProposalResponseDTO(@Schema(description = "Id da proposta", example = "1")
                                  Long id,
                                  DeadlineResponseDTO deadline,
                                  @Schema(description = "Valor ofertado", example = "3.000")
                                  BigDecimal offeredValue,
                                  @Schema(description = "Data da proposta", example = "2025-04-10T10:00:00")
                                  LocalDateTime createdAt,
                                  @Schema(description = "Perfil resumido do Freelancer criador da proposta")
                                  OtherUserProfileDTO freelancerProfile
) implements Serializable {
    public ProposalResponseDTO(Proposal proposal){
        this(
                proposal.getId(),
                DeadlineResponseDTO.fromEntity(proposal.getDeadline()),
                proposal.getOfferedValue(),
                proposal.getCreatedAt(),
                new OtherUserProfileDTO(
                        proposal.getFreelancer().getFullName(),
                        proposal.getFreelancer().getMainUserRole().name(),
                        proposal.getFreelancer().getCurrentUserRole().name()
                )
        );
    }
}
