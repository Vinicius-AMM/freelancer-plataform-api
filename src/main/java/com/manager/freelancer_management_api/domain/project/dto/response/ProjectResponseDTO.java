package com.manager.freelancer_management_api.domain.project.dto.response;

import com.manager.freelancer_management_api.domain.global.dto.DeadlineResponseDTO;
import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.user.dto.response.OtherUserProfileDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO para representar um projeto")
public record ProjectResponseDTO(@Schema(example = "Desenvolvimento de API")
                                 String title,
                                 @Schema(example = "Preciso de uma API de gerenciamento de freelancers.")
                                 String description,
                                 DeadlineResponseDTO deadline,
                                 @Schema(example = "3000.00")
                                 BigDecimal estimatedBudget,
                                 @Schema(example = "OPEN")
                                 ProjectStatus status,
                                 @Schema(example = "2025-04-10T10:00:00")
                                 LocalDateTime createdAt,
                                 @Schema(description = "perfil resumido do dono do projeto")
                                 OtherUserProfileDTO projectOwnerProfile
) {
    public ProjectResponseDTO(Project project) {
        this(
                project.getTitle(),
                project.getDescription(),
                DeadlineResponseDTO.fromEntity(project.getDeadline()),
                project.getEstimatedBudget(),
                project.getStatus(),
                project.getCreatedAt(),
                new OtherUserProfileDTO(
                        project.getUser().getFullName(),
                        project.getUser().getMainUserRole().name(),
                        project.getUser().getCurrentUserRole().name()
                )
        );
    }
}