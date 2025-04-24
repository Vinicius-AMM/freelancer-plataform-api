package com.manager.freelancer_management_api.domain.project.service.impl;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.dto.request.CreateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.request.UpdateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.project.exceptions.ProjectNotFoundException;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.domain.project.utils.ProjectAccessHelper;
import com.manager.freelancer_management_api.domain.project.utils.ProjectUpdateHelper;
import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import com.manager.freelancer_management_api.domain.user.exceptions.UserNotFoundException;
import com.manager.freelancer_management_api.domain.user.service.IUserService;
import com.manager.freelancer_management_api.utils.validator.PasswordValidator;
import com.manager.freelancer_management_api.utils.validator.UserAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectAccessHelper projectAccessHelper;

    @Mock
    private ProjectUpdateHelper projectUpdateHelper;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private UserAccessValidator userAccessValidator;

    @Mock
    private IUserService userService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UUID userId;
    private Long projectId;
    private User testUser;
    private Project testProject;
    private CreateProjectRequestDTO createDto;
    private UpdateProjectRequestDTO updateDto;
    private Pageable defaultPageable;
    private Pageable customPageable;

    private final int DEFAULT_PAGE_SIZE = 10;
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = 1L;

        testUser = User.builder()
                .id(userId)
                .fullName("Test Client")
                .email("client@test.com")
                .password("encodedPassword")
                .mainUserRole(UserRole.CLIENT)
                .currentUserRole(UserRole.CLIENT)
                .build();

        createDto = new CreateProjectRequestDTO(
                "New Project",
                "Project Description",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(31),
                new BigDecimal("2500.00")
        );

        testProject = Project.builder()
                .id(projectId)
                .title(createDto.title())
                .description(createDto.description())
                .deadline(new Deadline(createDto.startDate(), createDto.endDate()))
                .estimatedBudget(createDto.estimatedBudget())
                .status(ProjectStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();

        updateDto = new UpdateProjectRequestDTO(
                "Updated Title",
                "Updated Description",
                null,
                LocalDate.now().plusDays(45),
                new BigDecimal("3000.00"),
                ProjectStatus.IN_PROGRESS
        );

        defaultPageable = PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    @DisplayName("create project should save project when user is found")
    void createProject_shouldSaveProject_whenUserIsFound() {
        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(userId);
        when(userService.getUser(userId)).thenReturn(testUser);

        projectService.createProject(createDto);

        verify(userAccessValidator, times(1)).getAuthenticatedUserId();
        verify(userService, times(1)).getUser(userId);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository, times(1)).save(projectCaptor.capture());

        Project savedProject = projectCaptor.getValue();
        assertNotNull(savedProject);
        assertEquals(createDto.title(), savedProject.getTitle());
        assertEquals(testUser, savedProject.getUser());
        assertNotNull(savedProject.getDeadline());
        assertEquals(createDto.startDate(), savedProject.getDeadline().getStartDate());
        assertEquals(createDto.endDate(), savedProject.getDeadline().getEndDate());
        assertNotNull(savedProject.getCreatedAt());
    }
    @Test
    @DisplayName("create project should throw exception when user is not found")
    void createProject_shouldThrowException_whenUserIsNotFound() {
        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(userId);
        when(userService.getUser(userId)).thenThrow(new UserNotFoundException());

        assertThrows(UserNotFoundException.class, () -> projectService.createProject(createDto));

        verify(userAccessValidator, times(1)).getAuthenticatedUserId();
        verify(userService, times(1)).getUser(userId);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("getAllProjects should return page of ProjectResponseDTO")
    void getAllProjects_shouldReturnPageOfProjectResponseDTO() {
        Page<Project> projectPage = new PageImpl<>(List.of(testProject), defaultPageable, 1);
        when(projectRepository.findAll(defaultPageable)).thenReturn(projectPage);

        lenient().when(projectAccessHelper.findProjectById(projectId)).thenReturn(testProject);

        Pageable unsortedPageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
        Page<ProjectResponseDTO> resultPage = projectService.getAllProjects(unsortedPageable);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());
        assertEquals(0, resultPage.getNumber());
        assertEquals(testProject.getTitle(), resultPage.getContent().get(0).title());

        verify(projectRepository, times(1)).findAll(defaultPageable);
        verify(projectAccessHelper, times(1)).findProjectById(projectId);
    }
    @Test
    @DisplayName("getAllProjects should return page of DTOs using custom pageable")
    void getAllProjects_shouldReturnPageOfDTOs_withCustomPageable() {
        Pageable customPageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "title"));
        Page<Project> projectPage = new PageImpl<>(List.of(testProject), customPageable, 1);
        when(projectRepository.findAll(customPageable)).thenReturn(projectPage);
        lenient().when(projectAccessHelper.findProjectById(projectId)).thenReturn(testProject);

        Page<ProjectResponseDTO> resultPage = projectService.getAllProjects(customPageable);

        assertNotNull(resultPage);
        assertEquals(6, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());
        assertEquals(5, resultPage.getSize());
        assertEquals(testProject.getTitle(), resultPage.getContent().get(0).title());

        verify(projectRepository, times(1)).findAll(customPageable);
        verify(projectAccessHelper, times(1)).findProjectById(projectId);
    }
    @Test
    @DisplayName("getAllProjects should return empty page when no projects found")
    void getAllProjects_ShouldReturnEmptyPage_WhenNoProjectExists(){
        Page<Project> emptyPage = new PageImpl<>(Collections.emptyList(), defaultPageable, 0);
        when(projectRepository.findAll(defaultPageable)).thenReturn(emptyPage);

        Pageable requestedPageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
        Page<ProjectResponseDTO> resultPage = projectService.getAllProjects(requestedPageable);

        assertNotNull(resultPage);
        assertTrue(resultPage.isEmpty());
        assertEquals(0, resultPage.getTotalElements());

        verify(projectRepository, times(1)).findAll(defaultPageable);
        verify(projectAccessHelper, never()).findProjectById(anyLong());
    }

    @Test
    @DisplayName("getProjectById should return Project ProjectResponseDTO when the project exists")
    void getProjectById_shouldReturnDTO_whenProjectExists() {
        when(projectAccessHelper.findProjectById(projectId)).thenReturn(testProject);

        ProjectResponseDTO result = projectService.getProjectById(projectId);

        assertNotNull(result);
        assertEquals(testProject.getId(), projectId);
        assertEquals(testProject.getTitle(), result.title());
        assertEquals(testProject.getDescription(), result.description());
        assertEquals(testProject.getEstimatedBudget(), result.estimatedBudget());
        assertEquals(testProject.getStatus(), result.status());
        assertEquals(testProject.getUser().getFullName(), result.projectOwnerProfile().fullName());
        assertNotNull(result.deadline());
        assertTrue(result.deadline().durationInDays() > 0);

        verify(projectAccessHelper, times(1)).findProjectById(projectId);
    }
    @Test
    @DisplayName("getProjectById should throw ProjectNotFoundException when project does not exists")
    void getProjectById_shouldThrowProjectNotFoundException_whenProjectNotFound() {
        when(projectAccessHelper.findProjectById(projectId)).thenThrow(new ProjectNotFoundException());

        assertThrows(ProjectNotFoundException.class, () -> projectService.getProjectById(projectId));

        verify(projectAccessHelper, times(1)).findProjectById(projectId);
    }

    @Test
    @DisplayName("updateProject should update and save the project when the data is valid and the user is the owner")
    void updateProject_shouldUpdateAndSave_whenDataIsValidAndUserIsOwner() {
        when(projectAccessHelper.findProjectAndValidateOwnership(projectId)).thenReturn(testProject);
        when(projectUpdateHelper.updateSimpleFields(any(Project.class), eq(updateDto))).thenReturn(true);
        when(projectUpdateHelper.updateDeadlineIfNecessary(any(Project.class), eq(updateDto))).thenReturn(true);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        projectService.updateProject(projectId, updateDto);

        verify(projectAccessHelper, times(1)).findProjectAndValidateOwnership(projectId);
        verify(projectUpdateHelper, times(1)).updateSimpleFields(testProject, updateDto);
        verify(projectUpdateHelper, times(1)).updateDeadlineIfNecessary(testProject, updateDto);
        verify(projectRepository, times(1)).save(testProject);
    }
    @Test
    @DisplayName("updateProject should throw ProjectNotFoundException if the project is not found")
    void updateProject_shouldThrowProjectNotFoundException_whenProjectNotFound() {
        when(projectAccessHelper.findProjectAndValidateOwnership(projectId)).thenThrow(new ProjectNotFoundException());

        assertThrows(ProjectNotFoundException.class, () -> projectService.updateProject(projectId, updateDto));

        verify(projectAccessHelper, times(1)).findProjectAndValidateOwnership(projectId);
        verify(projectUpdateHelper, never()).updateSimpleFields(any(), any());
        verify(projectUpdateHelper, never()).updateDeadlineIfNecessary(any(), any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteProject should delete the project if user is owner and password is correct")
    void deleteProject_shouldDeleteProject_whenOwnerAndPasswordCorrect() {
        String rawPassword = "correctPassword";
        when(projectAccessHelper.findProjectAndValidateOwnership(projectId)).thenReturn(testProject);
        doNothing().when(passwordValidator).validate(rawPassword, testUser.getPassword(), "Senha incorreta. Não foi possível excluir o projeto.");
        doNothing().when(projectRepository).delete(testProject);

        projectService.deleteProject(projectId, rawPassword);

        verify(projectAccessHelper, times(1)).findProjectAndValidateOwnership(projectId);
        verify(passwordValidator, times(1)).validate(rawPassword, testUser.getPassword(), "Senha incorreta. Não foi possível excluir o projeto.");
        verify(projectRepository, times(1)).delete(testProject);
    }

    @Test
    @DisplayName("deleteProject should throws ProjectNotFoundException if project not found")
    void deleteProject_shouldThrowProjectNotFoundException_whenProjectNotFound() {
        String rawPassword = "password";
        when(projectAccessHelper.findProjectAndValidateOwnership(projectId)).thenThrow(new ProjectNotFoundException());

        assertThrows(ProjectNotFoundException.class, () -> projectService.deleteProject(projectId, rawPassword));

        verify(projectAccessHelper, times(1)).findProjectAndValidateOwnership(projectId);
        verify(passwordValidator, never()).validate(any(), any(), any());
        verify(projectRepository, never()).delete(any());
    }
}