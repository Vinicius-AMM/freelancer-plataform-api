package com.manager.freelancer_management_api.domain.project.service;

import com.manager.freelancer_management_api.domain.project.dto.request.*;
import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProjectService {
    void createProject(CreateProjectRequestDTO projectData);
    Page<ProjectResponseDTO> getAllProjects(Pageable pageable);
    ProjectResponseDTO getProjectById(Long projectId);
    void updateProject(Long projectId, UpdateProjectRequestDTO updateData);
    void deleteProject(Long projectId, String rawPassword);
    void markProjectAsCompletedByFreelancer(Long projectId, ProjectCompletionRequestDTO completionRequest);
    void approveProjectCompletionByClient(Long projectId, ApproveProjectRequestDTO approveProjectDTO);
    void requestProjectAdjustments(Long projectId, ProjectAdjustmentRequestDTO adjustmentRequest);
    Page<ProjectResponseDTO> getCompletedProjectsForClient(Pageable pageable);
    Page<ProjectResponseDTO> getCompletedProjectsForFreelancer(Pageable pageable);
    Page<ProjectResponseDTO> getProjectsPendingApprovalForClient(Pageable pageable);
    Page<ProjectResponseDTO> getProjectsInProgressAndNeedingAdjustmentsForFreelancer(Pageable pageable);
    Page<ProjectResponseDTO> getProjectsInProgressAndNeedingAdjustmentsForClient(Pageable pageable);
    Page<ProjectResponseDTO> getProjectsNeedingAdjustmentsForFreelancer(Pageable pageable);
}