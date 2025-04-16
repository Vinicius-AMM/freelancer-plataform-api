package com.manager.freelancer_management_api.domain.project.utils;

import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.exceptions.ProjectNotFoundException;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.utils.validator.UserAccessValidator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProjectAccessHelper {
    private final ProjectRepository projectRepository;
    private final UserAccessValidator userAccessValidator;

    public ProjectAccessHelper(ProjectRepository projectRepository, UserAccessValidator userAccessValidator) {
        this.projectRepository = projectRepository;
        this.userAccessValidator = userAccessValidator;
    }

    public Project findProjectAndValidateOwnership(Long projectId) {
        Project project = findProjectById(projectId);

        UUID ownerId = project.getUser().getId();
        userAccessValidator.validateAccess(ownerId);

        return project;
    }

    public Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
    }
}
