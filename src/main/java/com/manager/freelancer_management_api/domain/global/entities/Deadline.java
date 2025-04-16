package com.manager.freelancer_management_api.domain.global.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.LocalDate;

@Embeddable
@Data
public class Deadline {
    @Schema(example = "2025-04-10")
    private LocalDate startDate;
    @Schema(example = "2025-05-10")
    private LocalDate endDate;

    public Deadline(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
