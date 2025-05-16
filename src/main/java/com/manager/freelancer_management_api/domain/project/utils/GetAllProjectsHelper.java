package com.manager.freelancer_management_api.domain.project.utils;

import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.utils.common.PaginationHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GetAllProjectsHelper {
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private final PaginationHelper paginationHelper;

    public GetAllProjectsHelper(PaginationHelper paginationHelper) {
        this.paginationHelper = paginationHelper;
    }

    public Page<ProjectResponseDTO> getAllProjects(Pageable pageable, Function<Pageable, Page<Project>> fetchFunction){
        Pageable effectivePageable = paginationHelper.preparePageRequest(pageable, DEFAULT_SORT_FIELD);

        Page<Project> projectPage = fetchFunction.apply(effectivePageable);

        List<ProjectResponseDTO> list = projectPage.getContent().stream()
                .map(ProjectResponseDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(list, projectPage.getPageable(), projectPage.getTotalElements());
    }
}