package com.manager.freelancer_management_api.domain.project.service.impl;

import com.manager.freelancer_management_api.domain.global.exceptions.UnauthorizedAccessException;
import com.manager.freelancer_management_api.domain.project.dto.request.*;
import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.domain.project.service.IProjectService;
import com.manager.freelancer_management_api.domain.project.utils.GetAllProjectsHelper;
import com.manager.freelancer_management_api.domain.project.utils.ProjectAccessHelper;
import com.manager.freelancer_management_api.domain.project.utils.ProjectQueryBuilderHelper;
import com.manager.freelancer_management_api.domain.project.utils.ProjectUpdateHelper;
import com.manager.freelancer_management_api.domain.proposal.exception.ProjectNotAvailableException;
import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import com.manager.freelancer_management_api.domain.user.exceptions.UserNotFoundException;
import com.manager.freelancer_management_api.domain.user.service.IUserService;
import com.manager.freelancer_management_api.utils.validator.PasswordValidator;
import com.manager.freelancer_management_api.utils.validator.UserAccessValidator;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
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
    private final ProjectQueryBuilderHelper projectQueryBuilderHelper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectAccessHelper projectAccessHelper, ProjectUpdateHelper projectUpdateHelper, PasswordValidator passwordValidator, UserAccessValidator userAccessValidator, IUserService userService, GetAllProjectsHelper getAllProjectHelper, ProjectQueryBuilderHelper projectQueryBuilderHelper) {
        this.projectRepository = projectRepository;
        this.projectAccessHelper = projectAccessHelper;
        this.projectUpdateHelper = projectUpdateHelper;
        this.passwordValidator = passwordValidator;
        this.userAccessValidator = userAccessValidator;
        this.userService = userService;
        this.getAllProjectHelper = getAllProjectHelper;
        this.projectQueryBuilderHelper = projectQueryBuilderHelper;
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
        List<ProjectStatus> statuses = Arrays.asList(ProjectStatus.OPEN, ProjectStatus.NEGOTIATING);

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

        if(!(project.getStatus() == ProjectStatus.OPEN || project.getStatus() == ProjectStatus.NEGOTIATING)) {
            throw new ProjectNotAvailableException("Project can only be deleted if it is OPEN or NEGOTIATING. Current status: " + project.getStatus());
        }

        passwordValidator.validate(rawPassword,
                owner.getPassword(),
                "Invalid password. It was not possible to delete the project."
        );
        projectRepository.delete(project);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('FREELANCER')")
    public void markProjectAsCompletedByFreelancer(Long projectId, ProjectCompletionRequestDTO completionRequest) {
        UUID authenticatedUserId = userAccessValidator.getAuthenticatedUserId();
        Project project = projectAccessHelper.findProjectById(projectId);

        if(project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new ProjectNotAvailableException("Project is not in progress. Current status: " + project.getStatus());
        }
        if (project.getAcceptedFreelancer() == null) {
            throw new UserNotFoundException("No freelancer has been assigned to this project yet.");
        }
        if (!project.getAcceptedFreelancer().getId().equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException("Only the assigned freelancer can mark this project as completed.");
        }
        passwordValidator.validate(completionRequest.rawPassword(),
                project.getAcceptedFreelancer().getPassword(),
                "Invalid password. It was not possible to complete the project."
        );

        project.setStatus(ProjectStatus.COMPLETED_BY_FREELANCER);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    public void approveProjectCompletionByClient(Long projectId, ApproveProjectRequestDTO approveRequest) {
        Project project = projectAccessHelper.findProjectAndValidateOwnership(projectId);

        passwordValidator.validate(approveRequest.rawPassword(),
                project.getUser().getPassword(),
                "Invalid password. It was not possible to approve the project."
        );

        if(project.getStatus() != ProjectStatus.COMPLETED_BY_FREELANCER) {
            throw new ProjectNotAvailableException("Project is not completed by freelancer. Current status: " + project.getStatus());
        }
        
        project.setStatus(ProjectStatus.FINISHED);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    public void requestProjectAdjustments(Long projectId, ProjectAdjustmentRequestDTO adjustmentRequest) {
        Project project = projectAccessHelper.findProjectAndValidateOwnership(projectId);

        if (project.getStatus() != ProjectStatus.COMPLETED_BY_FREELANCER) {
            throw new ProjectNotAvailableException("Adjustments can only be requested for projects COMPLETED_BY_FREELANCER. Current status: " + project.getStatus());
        }
        passwordValidator.validate(adjustmentRequest.rawPassword(),
                project.getUser().getPassword(),
                "Invalid password. It was not possible to request project adjustments."
        );

        project.setStatus(ProjectStatus.NEEDING_ADJUSTMENTS);
        projectRepository.save(project);
    }

    private Page<ProjectResponseDTO> getProjectsForUserByStatus(
            Pageable pageable,
            List<ProjectStatus> statuses,
            UserRole perspective
    ) {
        UUID userId = userAccessValidator.getAuthenticatedUserId();
        Function<Pageable, Page<Project>> fetcher = projectQueryBuilderHelper.buildFetcherForUserByStatus(
                userId, statuses, perspective
        );
        return getAllProjectHelper.getAllProjects(pageable, fetcher);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    public Page<ProjectResponseDTO> getCompletedProjectsForClient(Pageable pageable) {
        return getProjectsForUserByStatus(pageable, Collections.singletonList(ProjectStatus.FINISHED), UserRole.CLIENT);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('FREELANCER')")
    public Page<ProjectResponseDTO> getCompletedProjectsForFreelancer(Pageable pageable) {
        return getProjectsForUserByStatus(pageable, Collections.singletonList(ProjectStatus.FINISHED), UserRole.FREELANCER);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    public Page<ProjectResponseDTO> getProjectsPendingApprovalForClient(Pageable pageable) {
        return getProjectsForUserByStatus(pageable, Collections.singletonList(ProjectStatus.COMPLETED_BY_FREELANCER), UserRole.CLIENT);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('FREELANCER')")
    public Page<ProjectResponseDTO> getProjectsInProgressAndNeedingAdjustmentsForFreelancer(Pageable pageable) {
        List<ProjectStatus> statuses = Arrays.asList(ProjectStatus.IN_PROGRESS, ProjectStatus.NEEDING_ADJUSTMENTS);
        return getProjectsForUserByStatus(pageable, statuses, UserRole.FREELANCER);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    public Page<ProjectResponseDTO> getProjectsInProgressAndNeedingAdjustmentsForClient(Pageable pageable) {
        List<ProjectStatus> statuses = Arrays.asList(ProjectStatus.IN_PROGRESS, ProjectStatus.NEEDING_ADJUSTMENTS);
        return getProjectsForUserByStatus(pageable, statuses, UserRole.CLIENT);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('FREELANCER')")
    public Page<ProjectResponseDTO> getProjectsNeedingAdjustmentsForFreelancer(Pageable pageable) {
        return getProjectsForUserByStatus(pageable, Collections.singletonList(ProjectStatus.NEEDING_ADJUSTMENTS), UserRole.FREELANCER);
    }
}