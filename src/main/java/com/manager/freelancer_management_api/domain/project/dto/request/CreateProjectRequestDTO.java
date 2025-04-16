package com.manager.freelancer_management_api.domain.project.dto.request;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "DTO para Requisição de criação de projeto")
public record CreateProjectRequestDTO(@Schema(description = "Título do projeto", example = "Desenvolvimento de API")
                                      @Size(max = 150, message = "Title cannot exceed 150 characters")
                                      String title,

                                      @Schema(description = "Descrição detalhada do projeto", example = "Preciso de uma API de gerenciamento de freelancers.")
                                      @NotBlank(message = "Description cannot be blank")
                                      String description,

                                      @Schema(description = "Data de início do projeto", example = "2025-04-10")
                                      @NotNull(message = "Start date cannot be null")
                                      @FutureOrPresent(message = "Start date must be in the present or future")
                                      LocalDate startDate,

                                      @Schema(description = "Data de fim do projeto", example = "2025-05-10")
                                      @NotNull(message = "End date cannot be null")
                                      @Future(message = "End date must be in the future")
                                      LocalDate endDate,

                                      @Schema(description = "Orçamento estimado para o projeto", example = "3000.00")
                                      @NotNull(message = "Estimated Budget date cannot be null")
                                      @Positive(message = "Estimated Budget must be greater than zero")
                                      BigDecimal estimatedBudget) {

    public Project toEntity(CreateProjectRequestDTO dto, User user){
        return Project.builder()
                .title(dto.title())
                .description(dto.description())
                .deadline(new Deadline(dto.startDate(), dto.endDate()))
                .estimatedBudget(dto.estimatedBudget())
                .status(ProjectStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .user(user).build();
    }
}
