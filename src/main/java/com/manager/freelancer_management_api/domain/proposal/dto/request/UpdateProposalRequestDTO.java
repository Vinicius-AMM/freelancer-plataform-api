package com.manager.freelancer_management_api.domain.proposal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProposalRequestDTO(@Schema(description = "Nova data de inicio", example = "2028-06-25")
                                       LocalDate newStartDate,
                                       @Schema(description = "Nova data de fim", example = "2028-07-25")
                                       LocalDate newEndDate,
                                       @Schema(description = "Novo valor ofertado", example = "5000.00")
                                       BigDecimal newOfferedValue) {
}