package com.manager.freelancer_management_api.domain.global.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Deadline {
    @Schema(example = "2028-05-10")
    private LocalDate startDate;
    @Schema(example = "2028-06-10")
    private LocalDate endDate;

    public Deadline(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
