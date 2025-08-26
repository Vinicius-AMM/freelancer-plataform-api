package com.manager.freelancer_management_api.domain.project.utils;

import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Component
public class ProjectQueryBuilderHelper {
    private final ProjectRepository projectRepository;

    public ProjectQueryBuilderHelper(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Function<Pageable, Page<Project>> buildFetcherForUserByStatus(
            UUID userId,
            List<ProjectStatus> statuses,
            UserRole perspective
    ) {
        if (perspective == UserRole.CLIENT) {
            if (statuses.size() == 1) {
                return effectivePageable -> projectRepository.findByStatusAndUserId(statuses.get(0), userId, effectivePageable);
            } else {
                return effectivePageable -> projectRepository.findByStatusInAndUserId(statuses, userId, effectivePageable);
            }
        } else {
            if (statuses.size() == 1) {
                return effectivePageable -> projectRepository.findByStatusAndAcceptedFreelancerId(statuses.get(0), userId, effectivePageable);
            } else {
                return effectivePageable -> projectRepository.findByStatusInAndAcceptedFreelancerId(statuses, userId, effectivePageable);
            }
        }
    }
}
