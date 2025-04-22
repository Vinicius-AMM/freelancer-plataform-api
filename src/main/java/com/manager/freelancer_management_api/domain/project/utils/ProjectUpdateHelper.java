package com.manager.freelancer_management_api.domain.project.utils;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.dto.request.UpdateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.exceptions.InvalidDateException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ProjectUpdateHelper {

    public boolean updateSimpleFields(Project project, UpdateProjectRequestDTO updateData) {
        boolean updated = false;

        if(updateData.title() != null) {
            project.setTitle(updateData.title());
            updated = true;
        }
        if(updateData.description() != null) {
            project.setDescription(updateData.description());
            updated = true;
        }
        if (updateData.estimatedBudget() != null) {
            project.setEstimatedBudget(updateData.estimatedBudget());
            updated = true;
        }
        if (updateData.projectStatus() != null) {
            project.setStatus(updateData.projectStatus());
            updated = true;
        }
        return updated;
    }

    public boolean updateDeadlineIfNecessary(Project project, UpdateProjectRequestDTO updateData) {
        LocalDate newStartDate = updateData.startDate();
        LocalDate newEndDate = updateData.endDate();

        if (newStartDate == null && newEndDate == null) return false;

        LocalDate currentStartDate = project.getDeadline() != null ? project.getDeadline().getStartDate() : null;
        LocalDate currentEndDate = project.getDeadline() != null ? project.getDeadline().getEndDate() : null;

        LocalDate effectiveStartDate = newStartDate != null ? newStartDate : currentStartDate;
        LocalDate effectiveEndDate = newEndDate != null ? newEndDate : currentEndDate;

        if (effectiveStartDate == null || effectiveEndDate == null) {
            throw new InvalidDateException("Ambas as datas (início e fim) são necessárias para atualizar o prazo.");
        }
        if (!effectiveEndDate.isAfter(effectiveStartDate)) {
            throw new InvalidDateException("A data final deve ser posterior à data de início.");
        }

        project.setDeadline(new Deadline(effectiveStartDate, effectiveEndDate));
        return true;

    }
}
