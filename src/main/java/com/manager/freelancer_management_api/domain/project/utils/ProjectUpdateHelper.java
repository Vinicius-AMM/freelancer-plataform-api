package com.manager.freelancer_management_api.domain.project.utils;

import com.manager.freelancer_management_api.domain.project.dto.request.UpdateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.utils.common.DeadlineUpdateUtil;
import org.springframework.stereotype.Component;

@Component
public class ProjectUpdateHelper {

    public ProjectUpdateHelper(){}

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
        if(updateData.startDate() == null && updateData.endDate() == null) {
            return false;
        }
        DeadlineUpdateUtil.applyDeadlineUpdateLogic(
                project::getDeadline,
                project::setDeadline,
                updateData.startDate(),
                updateData.endDate()
        );
        return true;
    }
}
