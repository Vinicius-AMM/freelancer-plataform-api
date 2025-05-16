package com.manager.freelancer_management_api.domain.project.utils;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import com.manager.freelancer_management_api.utils.common.PaginationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllProjectsHelperTest {
    @Mock
    private PaginationHelper paginationHelper;

    @InjectMocks
    private GetAllProjectsHelper getAllProjectsHelper;

    private Pageable initialPageable;
    private Pageable effectivePageable;
    private Project project1, project2;
    private static final String DEFAULT_SORT_FIELD = "createdAt";

   @BeforeEach
    void setUp() {
       initialPageable = PageRequest.of(0, 10);
       effectivePageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, DEFAULT_SORT_FIELD));
       User user = User.builder()
               .id(UUID.randomUUID())
               .fullName("Owner Test")
               .email("owner@test.com")
               .document("12345678901")
               .mainUserRole(UserRole.CLIENT)
               .currentUserRole(UserRole.CLIENT)
               .build();

       project1 = Project.builder()
               .id(1L)
               .title("Project 1")
               .description("Description 1")
               .deadline(new Deadline(LocalDate.now(), LocalDate.now().plusDays(30)))
               .estimatedBudget(new BigDecimal("1000.00"))
               .status(ProjectStatus.OPEN)
               .createdAt(LocalDateTime.now())
               .user(user)
               .build();

       project2 = Project.builder()
               .id(2L)
               .title("Project 2")
               .description("Description 2")
               .deadline(new Deadline(LocalDate.now(), LocalDate.now().plusDays(30)))
               .estimatedBudget(new BigDecimal("2000.00"))
               .status(ProjectStatus.NEGOTIATING)
               .createdAt(LocalDateTime.now())
               .user(user)
               .build();
   }

   @Test
   @DisplayName("getAllProjects should return page of ProjectResponseDTO successfully")
   void getAllProjects_shouldReturnPagedProjectResponseDTOs() {
       List<Project> projects = List.of(project1, project2);
       Page<Project> projectPage = new PageImpl<>(projects, effectivePageable, projects.size());

       when(paginationHelper.preparePageRequest(initialPageable, DEFAULT_SORT_FIELD)).thenReturn(effectivePageable);

       Function<Pageable, Page<Project>> fetcher = (pg) -> {
           if (pg.equals(effectivePageable)) {
               return projectPage;
           }
           return Page.empty();
       };

       Page<ProjectResponseDTO> resultPage = getAllProjectsHelper.getAllProjects(initialPageable, fetcher);

       assertNotNull(resultPage);
       assertEquals(2, resultPage.getTotalElements());
       assertEquals(2, resultPage.getContent().size());
       assertEquals(effectivePageable, resultPage.getPageable());

       List<String> resultTitles = resultPage.getContent().stream().map(ProjectResponseDTO::title).collect(Collectors.toList());
       assertTrue(resultTitles.contains("Project 1"));
       assertTrue(resultTitles.contains("Project 2"));

       ProjectResponseDTO resultDto1 = resultPage.getContent().stream().filter(dto -> dto.title().equals("Project 1")).findFirst().orElse(null);
       assertNotNull(resultDto1);
       assertEquals(project1.getDescription(), resultDto1.description());
       assertEquals(project1.getUser().getFullName(), resultDto1.projectOwnerProfile().fullName());

       verify(paginationHelper).preparePageRequest(initialPageable, DEFAULT_SORT_FIELD);
   }

    @Test
    @DisplayName("getAllProjects should return empty page if the fetcher returns empty")
    void getAllProjects_shouldReturnEmptyPage_whenFetcherReturnsEmpty() {
        Page<Project> emptyProjectPage = Page.empty(effectivePageable);

        when(paginationHelper.preparePageRequest(initialPageable, DEFAULT_SORT_FIELD)).thenReturn(effectivePageable);

        Function<Pageable, Page<Project>> fetcher = (pg) -> emptyProjectPage;

        Page<ProjectResponseDTO> resultPage = getAllProjectsHelper.getAllProjects(initialPageable, fetcher);

        assertNotNull(resultPage);
        assertTrue(resultPage.isEmpty());
        assertEquals(0, resultPage.getTotalElements());

        verify(paginationHelper).preparePageRequest(initialPageable, DEFAULT_SORT_FIELD);
    }
}