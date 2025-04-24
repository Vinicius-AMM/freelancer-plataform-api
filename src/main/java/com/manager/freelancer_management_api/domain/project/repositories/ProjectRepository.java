package com.manager.freelancer_management_api.domain.project.repositories;

import com.manager.freelancer_management_api.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
