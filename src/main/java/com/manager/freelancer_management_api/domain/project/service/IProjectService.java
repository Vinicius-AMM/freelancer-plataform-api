package com.manager.freelancer_management_api.domain.project.service;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.dto.request.CreateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IProjectService {
    void createProject(CreateProjectRequestDTO projectData);
    Page<ProjectResponseDTO> getAllProjects(Pageable pageable);
    ProjectResponseDTO getProjectById(Long projectId);
    void updateTitle(Long projectId, String newTitle);
    void updateDescription(Long projectId, String newDescription);
    void updateDeadline(Long projectId, Deadline newDeadline);
    void updateEstimatedBudget(Long projectId, BigDecimal newEstimatedBudget);
    void updateStatus(Long projectId, ProjectStatus newStatus);
    void deleteProject(Long projectId, String rawPassword);
}
