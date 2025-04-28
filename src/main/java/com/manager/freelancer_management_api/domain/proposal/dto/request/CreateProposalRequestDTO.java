package com.manager.freelancer_management_api.domain.proposal.dto.request;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import com.manager.freelancer_management_api.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO para criação do projeto.")
public record CreateProposalRequestDTO(@Schema(description = "Data de inicio", example = "2028-06-25")
                                       LocalDate startDate,
                                       @Schema(description = "Data de fim", example = "2028-07-25")
                                       LocalDate endDate,
                                       @Schema(description = "Valor de oferta", example = "3000.00")
                                       BigDecimal offeredValue
) {
    public Proposal toEntity(CreateProposalRequestDTO dto, Project project, User userFreelancer) {
        return Proposal.builder()
                .deadline(new Deadline(dto.startDate(), dto.endDate()))
                .offeredValue(dto.offeredValue())
                .project(project)
                .freelancer(userFreelancer)
                .build();
    }
}