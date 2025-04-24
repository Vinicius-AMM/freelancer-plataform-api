package com.manager.freelancer_management_api.domain.project.dto.request;

import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProjectRequestDTO(
        @Schema(description = "Novo título do projeto", example = "Refatoração da API", nullable = true)
        @Size(max = 150, message = "The title cannot exceed 150 characters.")
        String title,
        @Schema(description = "Nova descrição detalhada do projeto", example = "Atualizar a API para usar novas práticas.", nullable = true)
        String description,
        @Schema(description = "Nova data de início do projeto", example = "2025-06-01", nullable = true)
        @FutureOrPresent(message = "The start date must be in the present or future.")
        LocalDate startDate,
        @Schema(description = "Nova data de fim do projeto", example = "2025-07-01", nullable = true)
        @Future(message = "The final date must be in the future.")
        LocalDate endDate,
        @Schema(description = "Novo orçamento estimado para o projeto", example = "3500.00", nullable = true)
        @Positive(message = "The estimated budget must be greater than zero.")
        BigDecimal estimatedBudget,
        @Schema(description = "Novo status do projeto (ex: OPEN, IN_PROGRESS)", example = "IN_PROGRESS", nullable = true)
        ProjectStatus projectStatus
) {
}
