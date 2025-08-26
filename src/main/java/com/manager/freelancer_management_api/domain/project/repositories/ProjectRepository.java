package com.manager.freelancer_management_api.domain.project.repositories;

import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByStatusIn(List<ProjectStatus> statuses, Pageable pageable);
    Page<Project> findByStatusAndUserId(ProjectStatus status, UUID userId, Pageable pageable);
    Page<Project> findByStatusInAndUserId(List<ProjectStatus> statuses, UUID userId, Pageable pageable);
    Page<Project> findByStatusInAndAcceptedFreelancerId(List<ProjectStatus> statuses, UUID acceptedFreelancerId, Pageable pageable);
    Page<Project> findByStatusAndAcceptedFreelancerId(ProjectStatus status, UUID acceptedFreelancerId, Pageable pageable);
}
