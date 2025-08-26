package com.manager.freelancer_management_api.domain.project.service.impl;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.global.exceptions.UnauthorizedAccessException;
import com.manager.freelancer_management_api.domain.project.dto.request.*;
import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.project.exceptions.ProjectNotFoundException;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.domain.project.utils.GetAllProjectsHelper;
import com.manager.freelancer_management_api.domain.project.utils.ProjectAccessHelper;
import com.manager.freelancer_management_api.domain.project.utils.ProjectQueryBuilderHelper;
import com.manager.freelancer_management_api.domain.project.utils.ProjectUpdateHelper;
import com.manager.freelancer_management_api.domain.proposal.exception.ProjectNotAvailableException;
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
import java.util.UUID;
import java.util.function.Function;

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

    @Mock
    private ProjectQueryBuilderHelper projectQueryBuilderHelper;

    @Mock
    private GetAllProjectsHelper getAllProjectsHelper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UUID userId, freelancerUserId, otherFreelancerUserId;
    private Long projectId;
    private User testUserClient, testUserFreelancer, testUserOtherFreelancer;
    private Project testProject;
    private Pageable defaultPageable;
    private Pageable customPageable;

    private CreateProjectRequestDTO createDto;
    private UpdateProjectRequestDTO updateDto;
    private ProjectCompletionRequestDTO completionRequestDto;
    private ApproveProjectRequestDTO approveRequestDto;
    private ProjectAdjustmentRequestDTO adjustmentRequestDto;

    private final int DEFAULT_PAGE_SIZE = 10;
    private final String DEFAULT_PASSWORD = "password";
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        freelancerUserId = UUID.randomUUID();
        otherFreelancerUserId = UUID.randomUUID();
        projectId = 1L;

        testUserClient = User.builder()
                .id(userId)
                .fullName("Test Client")
                .email("client@test.com")
                .password("encodedPassword")
                .mainUserRole(UserRole.CLIENT)
                .currentUserRole(UserRole.CLIENT)
                .build();

        testUserFreelancer = User.builder()
                .id(freelancerUserId)
                .fullName("Test Freelancer")
                .email("freelancer@test.com")
                .password("encodedFreelancerPassword")
                .mainUserRole(UserRole.FREELANCER)
                .currentUserRole(UserRole.FREELANCER)
                .build();

        testUserOtherFreelancer = User.builder()
                .id(otherFreelancerUserId)
                .fullName("Other Freelancer")
                .email("otherfreelancer@test.com")
                .password("encodedOtherFreelancerPassword")
                .mainUserRole(UserRole.FREELANCER)
                .currentUserRole(UserRole.FREELANCER)
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
                .user(testUserClient)
                .build();

        updateDto = new UpdateProjectRequestDTO(
                "Updated Title",
                "Updated Description",
                null,
                LocalDate.now().plusDays(45),
                new BigDecimal("3000.00"),
                ProjectStatus.IN_PROGRESS
        );

        completionRequestDto = new ProjectCompletionRequestDTO(DEFAULT_PASSWORD, "Project delivered with all features.");
        approveRequestDto = new ApproveProjectRequestDTO(DEFAULT_PASSWORD);
        adjustmentRequestDto = new ProjectAdjustmentRequestDTO(DEFAULT_PASSWORD, "Need to adjust feature X.");

        defaultPageable = PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    @DisplayName("create project should save project when user is found")
    void createProject_shouldSaveProject_whenUserIsFound() {
        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(userId);
        when(userService.getUser(userId)).thenReturn(testUserClient);

        projectService.createProject(createDto);

        verify(userAccessValidator, times(1)).getAuthenticatedUserId();
        verify(userService, times(1)).getUser(userId);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository, times(1)).save(projectCaptor.capture());

        Project savedProject = projectCaptor.getValue();
        assertNotNull(savedProject);
        assertEquals(createDto.title(), savedProject.getTitle());
        assertEquals(testUserClient, savedProject.getUser());
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
        doNothing().when(passwordValidator).validate(rawPassword, testUserClient.getPassword(), "Invalid password. It was not possible to delete the project.");
        doNothing().when(projectRepository).delete(testProject);

        projectService.deleteProject(projectId, rawPassword);

        verify(projectAccessHelper, times(1)).findProjectAndValidateOwnership(projectId);
        verify(passwordValidator, times(1)).validate(rawPassword, testUserClient.getPassword(), "Invalid password. It was not possible to delete the project.");
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

    @Test
    @DisplayName("markProjectAsCompletedByFreelancer Should set status to COMPLETED_BY_FREELANCER and save")
    void markProjectAsCompleted_Success() {
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setAcceptedFreelancer(testUserFreelancer);

        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(freelancerUserId);
        when(projectAccessHelper.findProjectById(projectId)).thenReturn(testProject);
        doNothing().when(passwordValidator).validate(completionRequestDto.rawPassword(), testUserFreelancer.getPassword(), "Invalid password. It was not possible to complete the project.");

        projectService.markProjectAsCompletedByFreelancer(projectId, completionRequestDto);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();

        assertEquals(ProjectStatus.COMPLETED_BY_FREELANCER, savedProject.getStatus());
    }

    @Test
    @DisplayName("markProjectAsCompletedByFreelancer Should throw ProjectNotAvailableException if project not IN_PROGRESS")
    void markProjectAsCompleted_ThrowsProjectNotAvailableException_IfNotInProgress() {
        testProject.setStatus(ProjectStatus.OPEN);
        testProject.setAcceptedFreelancer(testUserFreelancer);

        when(projectAccessHelper.findProjectById(projectId)).thenReturn(testProject);

        assertThrows(ProjectNotAvailableException.class,
                () -> projectService.markProjectAsCompletedByFreelancer(projectId, completionRequestDto));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("markProjectAsCompletedByFreelancer Should throw UserNotFoundException if no accepted freelancer")
    void markProjectAsCompleted_ThrowsUserNotFoundException_IfNoAcceptedFreelancer() {
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setAcceptedFreelancer(null);

        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(freelancerUserId);
        when(projectAccessHelper.findProjectById(projectId)).thenReturn(testProject);

        assertThrows(UserNotFoundException.class,
                () -> projectService.markProjectAsCompletedByFreelancer(projectId, completionRequestDto));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("markProjectAsCompletedByFreelancer Should throw UnauthorizedAccessException if not the accepted freelancer")
    void markProjectAsCompleted_ThrowsUnauthorizedAccessException_IfNotAcceptedFreelancer() {
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setAcceptedFreelancer(testUserFreelancer);

        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(otherFreelancerUserId);
        when(projectAccessHelper.findProjectById(projectId)).thenReturn(testProject);

        assertThrows(UnauthorizedAccessException.class,
                () -> projectService.markProjectAsCompletedByFreelancer(projectId, completionRequestDto));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("approveProjectCompletionByClient Should set status to FINISHED and save")
    void approveProjectCompletion_Success() {
        testProject.setStatus(ProjectStatus.COMPLETED_BY_FREELANCER);
        testProject.setUser(testUserClient);

        when(projectAccessHelper.findProjectAndValidateOwnership(projectId)).thenReturn(testProject);
        doNothing().when(passwordValidator).validate(approveRequestDto.rawPassword(), testUserClient.getPassword(), "Invalid password. It was not possible to approve the project.");

        projectService.approveProjectCompletionByClient(projectId, approveRequestDto);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();

        assertEquals(ProjectStatus.FINISHED, savedProject.getStatus());
    }

    @Test
    @DisplayName("approveProjectCompletionByClient Should throw ProjectNotAvailableException if not COMPLETED_BY_FREELANCER")
    void approveProjectCompletion_ThrowsProjectNotAvailableException_IfNotCompletedByFreelancer() {
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setUser(testUserClient);

        when(projectAccessHelper.findProjectAndValidateOwnership(projectId)).thenReturn(testProject);

        assertThrows(ProjectNotAvailableException.class,
                () -> projectService.approveProjectCompletionByClient(projectId, approveRequestDto));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestProjectAdjustments Should set status to NEEDING_ADJUSTMENTS and save")
    void requestProjectAdjustments_Success() {
        testProject.setStatus(ProjectStatus.COMPLETED_BY_FREELANCER);
        testProject.setUser(testUserClient);

        when(projectAccessHelper.findProjectAndValidateOwnership(projectId)).thenReturn(testProject);
        doNothing().when(passwordValidator).validate(adjustmentRequestDto.rawPassword(), testUserClient.getPassword(), "Invalid password. It was not possible to request project adjustments.");

        projectService.requestProjectAdjustments(projectId, adjustmentRequestDto);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();

        assertEquals(ProjectStatus.NEEDING_ADJUSTMENTS, savedProject.getStatus());
    }

    @Test
    @DisplayName("requestProjectAdjustments Should throw ProjectNotAvailableException if not COMPLETED_BY_FREELANCER")
    void requestProjectAdjustments_ThrowsProjectNotAvailableException_IfNotCompletedByFreelancer() {
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setUser(testUserClient);

        when(projectAccessHelper.findProjectAndValidateOwnership(projectId)).thenReturn(testProject);

        assertThrows(ProjectNotAvailableException.class,
                () -> projectService.requestProjectAdjustments(projectId, adjustmentRequestDto));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCompletedProjectsForClient Should call getProjectsForUserByStatus with FINISHED status and CLIENT role")
    void getCompletedProjectsForClient_shouldCallHelperWithCorrectParams() {
        Page<ProjectResponseDTO> expectedPage = new PageImpl<>(Collections.emptyList());
        Function<Pageable, Page<Project>> mockFetcher = (page) -> Page.empty();

        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(userId);
        when(projectQueryBuilderHelper.buildFetcherForUserByStatus(
                eq(userId),
                eq(Collections.singletonList(ProjectStatus.FINISHED)),
                eq(UserRole.CLIENT))
        ).thenReturn(mockFetcher);
        when(getAllProjectsHelper.getAllProjects(eq(defaultPageable), eq(mockFetcher))).thenReturn(expectedPage);

        Page<ProjectResponseDTO> actualPage = projectService.getCompletedProjectsForClient(defaultPageable);

        assertEquals(expectedPage, actualPage);
        verify(userAccessValidator).getAuthenticatedUserId();
        verify(projectQueryBuilderHelper).buildFetcherForUserByStatus(
                userId,
                Collections.singletonList(ProjectStatus.FINISHED),
                UserRole.CLIENT
        );
        verify(getAllProjectsHelper).getAllProjects(defaultPageable, mockFetcher);
    }
}