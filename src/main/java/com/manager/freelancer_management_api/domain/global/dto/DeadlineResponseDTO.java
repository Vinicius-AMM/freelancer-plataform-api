package com.manager.freelancer_management_api.domain.global.dto;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record DeadlineResponseDTO(@Schema(example = "2025-04-01")
                                  LocalDate startDate,
                                  @Schema(example = "2025-05-01")
                                  LocalDate endDate,
                                  @Schema(example = "30")
                                  long durationInDays
) {
    public DeadlineResponseDTO(Deadline deadline){
        this(deadline.getStartDate(),
                deadline.getEndDate(),
                ChronoUnit.DAYS.between(deadline.getStartDate(), deadline.getEndDate()));
    }
    public static DeadlineResponseDTO fromEntity(Deadline deadline) {
        return new DeadlineResponseDTO(
                deadline.getStartDate(),
                deadline.getEndDate(),
                ChronoUnit.DAYS.between(deadline.getStartDate(), deadline.getEndDate())
        );
    }
}
