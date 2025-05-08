package com.manager.freelancer_management_api.domain.project.service.impl;

import com.manager.freelancer_management_api.domain.project.dto.request.CreateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.request.UpdateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.domain.project.service.IProjectService;
import com.manager.freelancer_management_api.domain.project.utils.GetAllProjectsHelper;
import com.manager.freelancer_management_api.domain.project.utils.ProjectAccessHelper;
import com.manager.freelancer_management_api.domain.project.utils.ProjectUpdateHelper;
import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.domain.user.service.IUserService;
import com.manager.freelancer_management_api.utils.validator.PasswordValidator;
import com.manager.freelancer_management_api.utils.validator.UserAccessValidator;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class ProjectServiceImpl implements IProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectAccessHelper projectAccessHelper;
    private final ProjectUpdateHelper projectUpdateHelper;
    private final PasswordValidator passwordValidator;
    private final UserAccessValidator userAccessValidator;
    private final IUserService userService;
    private final GetAllProjectsHelper getAllProjectHelper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectAccessHelper projectAccessHelper, ProjectUpdateHelper projectUpdateHelper, PasswordValidator passwordValidator, UserAccessValidator userAccessValidator, IUserService userService, GetAllProjectsHelper getAllProjectHelper) {
        this.projectRepository = projectRepository;
        this.projectAccessHelper = projectAccessHelper;
        this.projectUpdateHelper = projectUpdateHelper;
        this.passwordValidator = passwordValidator;
        this.userAccessValidator = userAccessValidator;
        this.userService = userService;
        this.getAllProjectHelper = getAllProjectHelper;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    public void createProject(CreateProjectRequestDTO projectData) {
        UUID authenticatedUserId = userAccessValidator.getAuthenticatedUserId();
        User projectOwner = userService.getUser(authenticatedUserId);
        Project project = projectData.toEntity(projectData, projectOwner);
        projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<ProjectResponseDTO> getAllProjects(Pageable pageable) {
        List<ProjectStatus> statuses = Arrays.asList(ProjectStatus.OPEN, ProjectStatus.IN_PROGRESS);

        Function<Pageable, Page<Project>> fetcher = effectivePageable ->
                projectRepository.findByStatusIn(statuses, effectivePageable);

        return getAllProjectHelper.getAllProjects(pageable, fetcher);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "getProjectCache", key = "#projectId")
    public ProjectResponseDTO getProjectById(Long projectId) {
        Project project = projectAccessHelper.findProjectById(projectId);
        return new ProjectResponseDTO(project);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    @CacheEvict(value = "getProjectCache", key = "#projectId")
    public void updateProject(Long projectId, UpdateProjectRequestDTO updateData) {
        Project project = projectAccessHelper.findProjectAndValidateOwnership(projectId);

        boolean updated = projectUpdateHelper.updateSimpleFields(project, updateData);
        updated |= projectUpdateHelper.updateDeadlineIfNecessary(project, updateData);

        if (updated) {
            projectRepository.save(project);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    @CacheEvict(value = "getProjectCache", key = "#projectId")
    public void deleteProject(Long projectId, String rawPassword) {
        Project project = projectAccessHelper.findProjectAndValidateOwnership(projectId);
        User owner = project.getUser();

        passwordValidator.validate(rawPassword,
                owner.getPassword(),
                "Invalid password. It was not possible to delete the project."
        );
        projectRepository.delete(project);
    }
}
