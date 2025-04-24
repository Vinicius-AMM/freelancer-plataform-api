package com.manager.freelancer_management_api.domain.project.service;

import com.manager.freelancer_management_api.domain.project.dto.request.CreateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.request.UpdateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProjectService {
    void createProject(CreateProjectRequestDTO projectData);
    Page<ProjectResponseDTO> getAllProjects(Pageable pageable);
    ProjectResponseDTO getProjectById(Long projectId);
    void updateProject(Long projectId, UpdateProjectRequestDTO updateData);
    void deleteProject(Long projectId, String rawPassword);
}
