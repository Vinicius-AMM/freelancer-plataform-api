package com.manager.freelancer_management_api.domain.proposal.util;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.global.exceptions.InvalidDateException;
import com.manager.freelancer_management_api.domain.proposal.dto.request.UpdateProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
class ProposalUpdateHelperTest {
    @InjectMocks
    private ProposalUpdateHelper proposalUpdateHelper;

    private Proposal proposal;
    private UpdateProposalRequestDTO fullUpdateDto;
    private UpdateProposalRequestDTO partialUpdateDto;
    private UpdateProposalRequestDTO emptyUpdateDto;
    private UpdateProposalRequestDTO deadlineOnlyUpdateDto;

    private LocalDate initialStartDate;
    private LocalDate initialEndDate;
    private BigDecimal initialOfferedValue;

    @BeforeEach
    void setUp() {
        initialStartDate = LocalDate.now().plusDays(1);
        initialEndDate = LocalDate.now().plusDays(30);
        initialOfferedValue = new BigDecimal("1000.00");

        proposal = Proposal.builder()
                .id(1L)
                .deadline(new Deadline(initialStartDate, initialEndDate))
                .offeredValue(initialOfferedValue)
                .createdAt(LocalDateTime.now())
                .build();

        fullUpdateDto = new UpdateProposalRequestDTO(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(40),
                new BigDecimal("1500.00")
        );

        partialUpdateDto = new UpdateProposalRequestDTO(
                null,
                LocalDate.now().plusDays(35),
                null
        );

        emptyUpdateDto = new UpdateProposalRequestDTO(null, null, null);

        deadlineOnlyUpdateDto = new UpdateProposalRequestDTO(
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(25),
                null
        );
    }

    @Test
    @DisplayName("updateOfferedValue should update offeredValue and return true when DTO has new value")
    void updateOfferedValue_shouldUpdateValueAndReturnTrue_whenDtoHasNewValue() {
        boolean updated = proposalUpdateHelper.updateOfferedValue(proposal, fullUpdateDto);

        assertTrue(updated);
        assertEquals(0, fullUpdateDto.newOfferedValue().compareTo(proposal.getOfferedValue()));
    }

    @Test
    @DisplayName("updateOfferedValue should not update offeredValue and return false when it has no new value")
    void updateOfferedValue_shouldNotUpdateValueAndReturnFalse_whenDtoHasNoNewValue() {
        boolean updated = proposalUpdateHelper.updateOfferedValue(proposal, partialUpdateDto);

        assertFalse(updated);
        assertEquals(0, initialOfferedValue.compareTo(proposal.getOfferedValue()));
    }

    @Test
    @DisplayName("updateDeadlineIfNecessary should update deadline and return true when new valid dates provided")
    void updateDeadlineIfNecessary_shouldUpdateDeadlineAndReturnTrue_whenNewValidDatesProvided() {
        boolean updated = proposalUpdateHelper.updateDeadlineIfNecessary(proposal, deadlineOnlyUpdateDto);

        assertTrue(updated);
        assertNotNull(proposal.getDeadline());
        assertEquals(deadlineOnlyUpdateDto.newStartDate(), proposal.getDeadline().getStartDate());
        assertEquals(deadlineOnlyUpdateDto.newEndDate(), proposal.getDeadline().getEndDate());
    }

    @Test
    @DisplayName("updateDeadlineIfNecessary should return false when both dates in DTO are null")
    void updateDeadlineIfNecessary_shouldReturnFalse_whenBothDtoDatesAreNull() {
        boolean updated = proposalUpdateHelper.updateDeadlineIfNecessary(proposal, emptyUpdateDto);
        assertFalse(updated);

        assertEquals(initialStartDate, proposal.getDeadline().getStartDate());
        assertEquals(initialEndDate, proposal.getDeadline().getEndDate());
    }

    @Test
    @DisplayName("updateDeadlineIfNecessary should update only endDate and return true when startDate in DTO is null")
    void updateDeadlineIfNecessary_shouldUpdateOnlyEndDateAndReturnTrue_whenDtoStartDateIsNull() {
        UpdateProposalRequestDTO dtoWithNullStartDate = new UpdateProposalRequestDTO(
                null, LocalDate.now().plusDays(50), null
        );
        boolean updated = proposalUpdateHelper.updateDeadlineIfNecessary(proposal, dtoWithNullStartDate);

        assertTrue(updated);
        assertNotNull(proposal.getDeadline());
        assertEquals(initialStartDate, proposal.getDeadline().getStartDate());
        assertEquals(dtoWithNullStartDate.newEndDate(), proposal.getDeadline().getEndDate());
    }

    @Test
    @DisplayName("updateDeadlineIfNecessary should throw InvalidDateException for invalid dates")
    void updateDeadlineIfNecessary_shouldThrowInvalidDateException_forInvalidDates() {
        UpdateProposalRequestDTO invalidDatesDto = new UpdateProposalRequestDTO(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(5),
                null
        );

        assertThrows(InvalidDateException.class, () -> {
            proposalUpdateHelper.updateDeadlineIfNecessary(proposal, invalidDatesDto);
        });
    }
    @Test
    @DisplayName("updateDeadlineIfNecessary should return false when new dates in DTO are same as current ones")
    void updateDeadlineIfNecessary_shouldReturnFalse_whenNewDatesAreSameAsCurrent() {
        UpdateProposalRequestDTO sameDatesDTO = new UpdateProposalRequestDTO(
                initialStartDate,
                initialEndDate,
                null
        );

        boolean updated = proposalUpdateHelper.updateDeadlineIfNecessary(proposal, sameDatesDTO);

        assertFalse(updated);
        assertEquals(sameDatesDTO.newStartDate(), proposal.getDeadline().getStartDate());
        assertEquals(sameDatesDTO.newEndDate(), proposal.getDeadline().getEndDate());
    }
}