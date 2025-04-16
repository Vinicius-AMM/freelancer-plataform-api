package com.manager.freelancer_management_api.domain.project.service.impl;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.dto.request.CreateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.domain.project.service.IProjectService;
import com.manager.freelancer_management_api.domain.project.utils.ProjectAccessHelper;
import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.domain.user.service.IUserService;
import com.manager.freelancer_management_api.utils.validator.PasswordValidator;
import com.manager.freelancer_management_api.utils.validator.UserAccessValidator;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@EnableCaching
public class ProjectServiceImpl implements IProjectService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ProjectRepository projectRepository;
    private final ProjectAccessHelper projectAccessHelper;
    private final PasswordValidator passwordValidator;
    private final UserAccessValidator userAccessValidator;
    private final IUserService userService;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectAccessHelper projectAccessHelper, PasswordValidator passwordValidator, UserAccessValidator userAccessValidator, IUserService userService) {
        this.projectRepository = projectRepository;
        this.projectAccessHelper = projectAccessHelper;
        this.passwordValidator = passwordValidator;
        this.userAccessValidator = userAccessValidator;
        this.userService = userService;
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
    @PreAuthorize("isAuthenticated()")
    public Page<ProjectResponseDTO> getAllProjects(Pageable pageable) {
        int pageSize = pageable.getPageSize() > 0 ? pageable.getPageSize() : DEFAULT_PAGE_SIZE;
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable effectivePageable = PageRequest.of(pageable.getPageNumber(), pageSize, sort);

        Page<Project> projects = projectRepository.findAll(effectivePageable);

        List<ProjectResponseDTO> projectsList = projects.getContent().stream()
                .map(project -> getProjectById(project.getId()))
                .collect(Collectors.toList());

        return new PageImpl<>(projectsList, projects.getPageable(), projects.getTotalElements());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "getProjectCache", key = "#projectId")
    public ProjectResponseDTO getProjectById(Long projectId) {
        Project project = projectAccessHelper.findProjectById(projectId);
        return new ProjectResponseDTO(project);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    @CachePut(value = "getProjectCache", key = "#projectId")
    public void updateTitle(Long projectId, String newTitle) {
        Project project = projectAccessHelper.findProjectAndValidateOwnership(projectId);
        project.setTitle(newTitle);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    @CachePut(value = "getProjectCache", key = "#projectId")
    public void updateDescription(Long projectId, String newDescription) {
        Project project = projectAccessHelper.findProjectAndValidateOwnership(projectId);
        project.setDescription(newDescription);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    @CachePut(value = "getProjectCache", key = "#projectId")
    public void updateDeadline(Long projectId, Deadline newDeadline) {
        Project project = projectAccessHelper.findProjectAndValidateOwnership(projectId);
        project.setDeadline(newDeadline);
        projectRepository.save(project);
    }

    @Override
    @PreAuthorize("hasRole('CLIENT')")
    @CachePut(value = "getProjectCache", key = "#projectId")
    public void updateEstimatedBudget(Long projectId, BigDecimal newEstimatedBudget) {
        Project project = projectAccessHelper.findProjectAndValidateOwnership(projectId);
        project.setEstimatedBudget(newEstimatedBudget);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    @CachePut(value = "getProjectCache", key = "#projectId")
    public void updateStatus(Long projectId, ProjectStatus newStatus) {
        Project project = projectAccessHelper.findProjectAndValidateOwnership(projectId);
        project.setStatus(newStatus);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    @Caching(evict = {
            @CacheEvict(value = "getProjectCache", key = "#projectId"),
            @CacheEvict(value = "getProjectsCache", key = "#projectId")
    })
    public void deleteProject(Long projectId, String rawPassword) {
        Project project = projectAccessHelper.findProjectAndValidateOwnership(projectId);
        User owner = project.getUser();

        passwordValidator.validate(rawPassword,
                owner.getPassword(),
                "Senha incorreta. Não foi possível excluir o projeto."
        );
        projectRepository.delete(project);
    }
}
