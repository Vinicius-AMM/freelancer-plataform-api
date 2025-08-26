package com.manager.freelancer_management_api.domain.project.utils;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.dto.request.UpdateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.global.exceptions.InvalidDateException;
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

@ExtendWith(MockitoExtension.class)
class ProjectUpdateHelperTest {
    @InjectMocks
    private ProjectUpdateHelper projectUpdateHelper;

    private Project project;
    private UpdateProjectRequestDTO fullUpdateDto;
    private UpdateProjectRequestDTO partialUpdateDto = new UpdateProjectRequestDTO(
            "Partial New Title",
            null,
            null,
            LocalDate.now().plusDays(35),
            null,
            ProjectStatus.NEGOTIATING
    );
    private UpdateProjectRequestDTO emptyUpdateDto;
    private UpdateProjectRequestDTO deadlineOnlyUpdateDto;

    private LocalDate initialStartDate;
    private LocalDate initialEndDate;

    @BeforeEach
    void setUp() {
        initialStartDate = LocalDate.now().plusDays(1);
        initialEndDate = LocalDate.now().plusDays(30);

        project = Project.builder()
                .id(1L)
                .title("Original Title")
                .description("Original Description")
                .deadline(new Deadline(initialStartDate, initialEndDate))
                .estimatedBudget(new BigDecimal("1000.00"))
                .status(ProjectStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        fullUpdateDto = new UpdateProjectRequestDTO(
                "New Title",
                "New Description",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(40),
                new BigDecimal("1500.00"),
                ProjectStatus.IN_PROGRESS
        );

        emptyUpdateDto = new UpdateProjectRequestDTO(null, null, null, null, null, null);

        deadlineOnlyUpdateDto = new UpdateProjectRequestDTO(
                null,
                null,
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(25),
                null,
                null
        );
    }

    @Test
    @DisplayName("updateSimpleFields should update all simple fields when full supplied")
    void updateSimpleFields_shouldUpdateAllFields_whenFullDtoProvided() {
        boolean updated = projectUpdateHelper.updateSimpleFields(project, fullUpdateDto);

        assertTrue(updated);
        assertEquals(fullUpdateDto.title(), project.getTitle());
        assertEquals(fullUpdateDto.description(), project.getDescription());
        assertEquals(0, fullUpdateDto.estimatedBudget().compareTo(project.getEstimatedBudget()));
        assertEquals(fullUpdateDto.projectStatus(), project.getStatus());
    }

    @Test
    @DisplayName("updateSimpleFields must update partial fields when partially provided")
    void updateSimpleFields_shouldUpdatePartialFields_whenPartialDtoProvided() {
        String originalDescription = project.getDescription();
        BigDecimal originalBudget = project.getEstimatedBudget();

        boolean updated = projectUpdateHelper.updateSimpleFields(project, partialUpdateDto);

        assertTrue(updated);
        assertEquals(partialUpdateDto.title(), project.getTitle());
        assertEquals(originalDescription, project.getDescription());
        assertEquals(0, originalBudget.compareTo(project.getEstimatedBudget()));
        assertEquals(partialUpdateDto.projectStatus(), project.getStatus());
    }

    @Test
    @DisplayName("updateSimpleFields should not update any field and return false when the empty")
    void updateSimpleFields_shouldNotUpdateAnyField_whenEmptyDtoProvided() {
        Project originalProjectState = Project.builder()
                .title(project.getTitle())
                .description(project.getDescription())
                .estimatedBudget(project.getEstimatedBudget())
                .status(project.getStatus())
                .build();

        boolean updated = projectUpdateHelper.updateSimpleFields(project, emptyUpdateDto);

        assertFalse(updated);
        assertEquals(originalProjectState.getTitle(), project.getTitle());
        assertEquals(originalProjectState.getDescription(), project.getDescription());
        assertEquals(0, originalProjectState.getEstimatedBudget().compareTo(project.getEstimatedBudget()));
        assertEquals(originalProjectState.getStatus(), project.getStatus());
    }

    @Test
    @DisplayName("updateDeadlineIfNecessary should update Deadline when new valid dates provided")
    void updateDeadlineIfNecessary_shouldUpdateDeadline_whenNewValidDatesProvided() {
        boolean updated = projectUpdateHelper.updateDeadlineIfNecessary(project, deadlineOnlyUpdateDto);
        Deadline originalDeadline = new Deadline(project.getDeadline().getStartDate(), project.getDeadline().getEndDate());
        projectUpdateHelper.updateDeadlineIfNecessary(project, deadlineOnlyUpdateDto);

        assertNotNull(project.getDeadline());
        assertEquals(deadlineOnlyUpdateDto.startDate(), project.getDeadline().getStartDate());
        assertEquals(deadlineOnlyUpdateDto.endDate(), project.getDeadline().getEndDate());
    }

    @Test
    @DisplayName("updateDeadlineIfNecessary You should only update endDate when StartDate do DTO is null")
    void updateDeadlineIfNecessary_shouldUpdateOnlyEndDate_whenDtoStartDateIsNull() {
        LocalDate newEndDate = LocalDate.of(2025, 07, 20);
        UpdateProjectRequestDTO dtoWithNullStartDate = new UpdateProjectRequestDTO(
                null, null, null, newEndDate, null, null
        );
        boolean updated = projectUpdateHelper.updateDeadlineIfNecessary(project, dtoWithNullStartDate);

        assertTrue(updated);
        assertNotNull(project.getDeadline());
        assertEquals(initialStartDate, project.getDeadline().getStartDate());
        assertEquals(newEndDate, project.getDeadline().getEndDate());
    }

    @Test
    @DisplayName("updateDeadlineIfNecessary should only update startDate when DTO endDate is null")
    void updateDeadlineIfNecessary_shouldUpdateOnlyStartDate_whenDtoEndDateIsNull() {
        UpdateProjectRequestDTO dtoWithNullEndDate = new UpdateProjectRequestDTO(
                null, null, LocalDate.now().plusDays(3), null, null, null
        );

        boolean updated = projectUpdateHelper.updateDeadlineIfNecessary(project, dtoWithNullEndDate);

        assertTrue(updated);
        assertNotNull(project.getDeadline());
        assertEquals(dtoWithNullEndDate.startDate(), project.getDeadline().getStartDate());
        assertEquals(initialEndDate, project.getDeadline().getEndDate());
    }

    @Test
    @DisplayName("updateDeadlineIfNecessary must return false when both dates in the DTO are null")
    void updateDeadlineIfNecessary_shouldReturnFalse_whenBothDtoDatesAreNull() {
        boolean updated = projectUpdateHelper.updateDeadlineIfNecessary(project, emptyUpdateDto);
        assertFalse(updated);

        assertEquals(initialStartDate, project.getDeadline().getStartDate());
        assertEquals(initialEndDate, project.getDeadline().getEndDate());
    }

    @Test
    @DisplayName("updateDeadlineIfNecessary should throw InvalidDateException for invalid dates")
    void updateDeadlineIfNecessary_shouldThrowInvalidDateException_forInvalidDates() {
        UpdateProjectRequestDTO invalidDatesDto = new UpdateProjectRequestDTO(
                null,
                null,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(5),
                null,
                null
        );

        assertThrows(InvalidDateException.class, () -> {
            projectUpdateHelper.updateDeadlineIfNecessary(project, invalidDatesDto);
        });
    }
}