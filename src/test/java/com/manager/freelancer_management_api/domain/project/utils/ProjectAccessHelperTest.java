package com.manager.freelancer_management_api.domain.project.utils;

import com.manager.freelancer_management_api.domain.global.exceptions.UnauthorizedAccessException;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.exceptions.ProjectNotFoundException;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.utils.validator.UserAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectAccessHelperTest {
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserAccessValidator userAccessValidator;

    @InjectMocks
    private ProjectAccessHelper projectAccessHelper;

    private Project project;
    private User owner;
    private Long projectId;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        projectId = 1L;
        ownerId = UUID.randomUUID();

        owner = User.builder().id(ownerId).build();
        project = Project.builder().id(projectId).user(owner).title("Test Project").build();
    }

    @Test
    @DisplayName("findProjectById should return project when found")
    void findProjectById_shouldReturnProject_whenFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        Project foundProject = projectAccessHelper.findProjectById(projectId);

        assertNotNull(foundProject);
        assertEquals(projectId, foundProject.getId());
        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("findProjectById should throw ProjectNotFoundException when not found")
    void findProjectById_shouldThrowProjectNotFoundException_whenNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectAccessHelper.findProjectById(projectId));
        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("findProjectAndValidateOwnership should return project when found e user is owner")
    void findProjectAndValidateOwnership_shouldReturnProject_whenFoundAndUserIsOwner() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(userAccessValidator).validateAccess(ownerId);

        Project foundProject = projectAccessHelper.findProjectAndValidateOwnership(projectId);

        assertNotNull(foundProject);
        assertEquals(projectId, foundProject.getId());
        verify(projectRepository).findById(projectId);
        verify(userAccessValidator).validateAccess(ownerId);
    }

    @Test
    @DisplayName("findProjectAndValidateOwnership sould throw ProjectNotFoundException when project not found")
    void findProjectAndValidateOwnership_shouldThrowProjectNotFoundException_whenProjectNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectAccessHelper.findProjectAndValidateOwnership(projectId));
        verify(projectRepository).findById(projectId);
        verify(userAccessValidator, never()).validateAccess(any(UUID.class));
    }

    @Test
    @DisplayName("findProjectAndValidateOwnership should throw UnauthorizedAccessException when user is not owner")
    void findProjectAndValidateOwnership_shouldThrowUnauthorizedAccessException_whenUserIsNotOwner() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doThrow(new UnauthorizedAccessException("Access denied.")).when(userAccessValidator).validateAccess(ownerId);

        assertThrows(UnauthorizedAccessException.class, () -> projectAccessHelper.findProjectAndValidateOwnership(projectId));
        verify(projectRepository).findById(projectId);
        verify(userAccessValidator).validateAccess(ownerId);
    }
}