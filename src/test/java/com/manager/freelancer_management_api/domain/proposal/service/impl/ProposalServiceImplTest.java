package com.manager.freelancer_management_api.domain.proposal.service.impl;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.domain.project.utils.ProjectAccessHelper;
import com.manager.freelancer_management_api.domain.proposal.dto.request.CreateProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.request.ProcessProposalDecisionRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.request.UpdateProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.response.ProposalResponseDTO;
import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import com.manager.freelancer_management_api.domain.proposal.enums.ProposalDecisionAction;
import com.manager.freelancer_management_api.domain.proposal.exception.ProjectNotAvailableException;
import com.manager.freelancer_management_api.domain.proposal.exception.ProposalNotFoundException;
import com.manager.freelancer_management_api.domain.proposal.repository.ProposalRepository;
import com.manager.freelancer_management_api.domain.proposal.util.GetAllProposalsHelper;
import com.manager.freelancer_management_api.domain.proposal.util.ProposalAccessHelper;
import com.manager.freelancer_management_api.domain.proposal.util.ProposalUpdateHelper;
import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import com.manager.freelancer_management_api.domain.user.exceptions.InvalidUserRoleException;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProposalServiceImplTest {
    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private ProposalAccessHelper proposalAccessHelper;
    @Mock
    private PasswordValidator passwordValidator;
    @Mock
    private UserAccessValidator userAccessValidator;
    @Mock
    private IUserService userService;
    @Mock
    private ProjectAccessHelper projectAccessHelper;
    @Mock
    private ProposalUpdateHelper proposalUpdateHelper;
    @Mock
    private GetAllProposalsHelper getAllProposalsHelper;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache projectCache;

    @InjectMocks
    private ProposalServiceImpl proposalService;

    private User freelancer;
    private User client;
    private Project project;
    private Proposal proposal, proposal2;
    private CreateProposalRequestDTO createDto;
    private UpdateProposalRequestDTO updateDto;
    private ProcessProposalDecisionRequestDTO acceptDecisionDto, declineDecisionDto;
    private UUID freelancerId;
    private UUID clientId;
    private Long projectId;
    private Long proposalId;

    @BeforeEach
    void setUp() {
        freelancer = User.builder()
                .id(UUID.randomUUID())
                .fullName("Freelancer Test")
                .email("freelancer@test.com")
                .password("password")
                .document("12345678901")
                .mainUserRole(UserRole.CLIENT)
                .currentUserRole(UserRole.FREELANCER)
                .build();

        client = User.builder()
                .id(UUID.randomUUID())
                .fullName("Client Test")
                .email("client@test.com")
                .password("password")
                .document("12345678901")
                .mainUserRole(UserRole.CLIENT)
                .currentUserRole(UserRole.CLIENT)
                .build();

        project = Project.builder()
                .id(1L)
                .title("Project 1")
                .description("Description 1")
                .deadline(new Deadline(LocalDate.now(), LocalDate.now().plusDays(30)))
                .estimatedBudget(new BigDecimal("1000.00"))
                .status(ProjectStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .user(client)
                .build();

        proposal = Proposal.builder()
                .id(1L)
                .deadline(new Deadline(LocalDate.now(), LocalDate.now().plusDays(30)))
                .offeredValue(new BigDecimal("1000.00"))
                .freelancer(freelancer)
                .project(project)
                .build();

        proposal2 = Proposal.builder()
                .id(2L)
                .deadline(new Deadline(LocalDate.now(), LocalDate.now().plusDays(30)))
                .offeredValue(new BigDecimal("1000.00"))
                .freelancer(freelancer)
                .project(project)
                .build();
        
        createDto = new CreateProposalRequestDTO(LocalDate.now().plusDays(2), LocalDate.now().plusDays(12), new BigDecimal("150"));
        updateDto = new UpdateProposalRequestDTO(LocalDate.now().plusDays(3), LocalDate.now().plusDays(13), new BigDecimal("180"));
        acceptDecisionDto = new ProcessProposalDecisionRequestDTO(ProposalDecisionAction.ACCEPT, "password");
        declineDecisionDto = new ProcessProposalDecisionRequestDTO(ProposalDecisionAction.DECLINE, "password");
        freelancerId = freelancer.getId();
        clientId = client.getId();
        projectId = project.getId();
        proposalId = proposal.getId();
    }

    @Test
    @DisplayName("createProposal should create proposal and chanege the project status for NEGOTIATING")
    void createProposal_shouldSaveProposal_whenProjectIsFound() {
        when(projectAccessHelper.findProjectById(projectId)).thenReturn(project);
        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(freelancerId);
        when(userService.getUser(freelancerId)).thenReturn(freelancer);
        when(proposalRepository.save(any(Proposal.class))).thenReturn(proposal);

        proposalService.createProposal(projectId, createDto);

        verify(projectAccessHelper).findProjectById(projectId);
        verify(userAccessValidator).getAuthenticatedUserId();
        verify(userService).getUser(freelancerId);
        ArgumentCaptor<Proposal> proposalCaptor = ArgumentCaptor.forClass(Proposal.class);
        verify(proposalRepository).save(proposalCaptor.capture());

        Proposal savedProposal = proposalCaptor.getValue();
        assertEquals(createDto.offeredValue(), savedProposal.getOfferedValue());
        assertEquals(project, savedProposal.getProject());
        assertEquals(freelancer, savedProposal.getFreelancer());
        assertEquals(ProjectStatus.NEGOTIATING, project.getStatus());
    }
    @Test
    @DisplayName("createProposal should throw InvalidUserRoleException if user is not FREELANCER")
    void createProposal_ThrowsInvalidUserRoleException_IfNotFreelancer() {
        when(projectAccessHelper.findProjectById(projectId)).thenReturn(project);
        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(clientId);
        when(userService.getUser(clientId)).thenReturn(client);

        assertThrows(InvalidUserRoleException.class, () -> proposalService.createProposal(projectId, createDto));
        verify(proposalRepository, never()).save(any());
    }
    @Test
    @DisplayName("getProposalById should return ProposalResponseDTO")
    void getProposalById_Success() {
        when(proposalAccessHelper.findProposalById(proposalId)).thenReturn(proposal);

        ProposalResponseDTO result = proposalService.getProposalById(proposalId);

        assertNotNull(result);
        assertEquals(proposal.getId(), result.id());
        assertEquals(proposal.getOfferedValue(), result.offeredValue());
        verify(proposalAccessHelper).findProposalById(proposalId);
    }
    @Test
    @DisplayName("getProposalById should throw ProposalNotFoundException if proposal is not found")
    void getProposalById_ThrowsProposalNotFoundException() {
        when(proposalAccessHelper.findProposalById(proposalId)).thenThrow(new ProposalNotFoundException());
        assertThrows(ProposalNotFoundException.class, () -> proposalService.getProposalById(proposalId));
    }
    @Test
    @DisplayName("updateProposal should update proposal if changes are made")
    void updateProposal_Success_IfChangesMade() {
        when(proposalAccessHelper.findProposalAndValidateOwnership(proposalId)).thenReturn(proposal);

        when(proposalUpdateHelper.updateOfferedValue(proposal, updateDto)).thenReturn(true);
        when(proposalUpdateHelper.updateDeadlineIfNecessary(proposal, updateDto)).thenReturn(false);
        when(proposalRepository.save(any(Proposal.class))).thenReturn(proposal);

        proposalService.updateProposal(proposalId, updateDto);

        verify(proposalAccessHelper).findProposalAndValidateOwnership(proposalId);
        verify(proposalUpdateHelper).updateOfferedValue(proposal, updateDto);
        verify(proposalUpdateHelper).updateDeadlineIfNecessary(proposal, updateDto);
        verify(proposalRepository).save(proposal);
    }
    @Test
    @DisplayName("updateProposal should not save if there are no changes")
    void updateProposal_NoSave_IfNoChangesMade() {
        when(proposalAccessHelper.findProposalAndValidateOwnership(proposalId)).thenReturn(proposal);
        when(proposalUpdateHelper.updateOfferedValue(proposal, updateDto)).thenReturn(false);
        when(proposalUpdateHelper.updateDeadlineIfNecessary(proposal, updateDto)).thenReturn(false);

        proposalService.updateProposal(proposalId, updateDto);

        verify(proposalAccessHelper).findProposalAndValidateOwnership(proposalId);
        verify(proposalUpdateHelper).updateOfferedValue(proposal, updateDto);
        verify(proposalUpdateHelper).updateDeadlineIfNecessary(proposal, updateDto);
        verify(proposalRepository, never()).save(any(Proposal.class));
    }
    @Test
    @DisplayName("deleteProposal should delete proposal with valid password")
    void deleteProposal_Success() {
        String rawPassword = "passwordFree";
        when(proposalAccessHelper.findProposalAndValidateOwnership(proposalId)).thenReturn(proposal);
        doNothing().when(passwordValidator).validate(rawPassword, freelancer.getPassword(), "Invalid password. It was not possible to delete the proposal.");

        proposalService.deleteProposal(proposalId, rawPassword);

        verify(proposalAccessHelper).findProposalAndValidateOwnership(proposalId);
        verify(passwordValidator).validate(rawPassword, freelancer.getPassword(), "Invalid password. It was not possible to delete the proposal.");
        verify(proposalRepository).delete(proposal);
    }
    @Test
    @DisplayName("processProposalDecision ACCEPT should change project status to IN_PROGRESS")
    void processProposalDecision_Accept_ChangesProjectStatusToInProgress() {
        when(cacheManager.getCache("getProjectCache")).thenReturn(projectCache);

        project.setStatus(ProjectStatus.NEGOTIATING);

        when(proposalAccessHelper.findProposalById(proposalId)).thenReturn(proposal);
        doNothing().when(userAccessValidator).validateAccess(clientId);
        doNothing().when(passwordValidator).validate(acceptDecisionDto.rawPassword(), client.getPassword(), "Invalid password. It was not possible to process the proposal decision.");
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        proposalService.processProposalDecision(proposalId, acceptDecisionDto);

        assertEquals(ProjectStatus.IN_PROGRESS, project.getStatus());
        verify(proposalAccessHelper).findProposalById(proposalId);
        verify(userAccessValidator).validateAccess(clientId);
        verify(passwordValidator).validate(acceptDecisionDto.rawPassword(), client.getPassword(), "Invalid password. It was not possible to process the proposal decision.");
        verify(proposalRepository, never()).delete(any());
    }
    @Test
    @DisplayName("processProposalDecision DECLINE should delete proposal")
    void processProposalDecision_Decline_DeletesProposal() {
        declineDecisionDto = new ProcessProposalDecisionRequestDTO(ProposalDecisionAction.DECLINE, "clientPassword");
        project.setStatus(ProjectStatus.OPEN);

        when(proposalAccessHelper.findProposalById(proposalId)).thenReturn(proposal);
        doNothing().when(userAccessValidator).validateAccess(clientId);
        doNothing().when(passwordValidator).validate(declineDecisionDto.rawPassword(), client.getPassword(), "Invalid password. It was not possible to process the proposal decision.");

        proposalService.processProposalDecision(proposalId, declineDecisionDto);

        verify(proposalRepository).delete(proposal);
        assertEquals(ProjectStatus.OPEN, project.getStatus());
    }

    @Test
    @DisplayName("processProposalDecision DECLINE in the last proposal in NEGOTIATING should change project status to OPEN")
    void processProposalDecision_DeclineLastProposal_ChangesProjectStatusToOpen() {
        when(cacheManager.getCache("getProjectCache")).thenReturn(projectCache);

        declineDecisionDto = new ProcessProposalDecisionRequestDTO(ProposalDecisionAction.DECLINE, "clientPassword");
        project.setStatus(ProjectStatus.NEGOTIATING);
        when(proposalRepository.countByProjectId(projectId)).thenReturn(0L);

        when(proposalAccessHelper.findProposalById(proposalId)).thenReturn(proposal);
        doNothing().when(userAccessValidator).validateAccess(clientId);
        doNothing().when(passwordValidator).validate(declineDecisionDto.rawPassword(), client.getPassword(), "Invalid password. It was not possible to process the proposal decision.");

        proposalService.processProposalDecision(proposalId, declineDecisionDto);

        verify(proposalRepository).delete(proposal);
        assertEquals(ProjectStatus.OPEN, project.getStatus());
    }
    @Test
    @DisplayName("processProposalDecision should throw ProjectNotAvailableException if project status is not OPEN or NEGOTIATING")
    void processProposalDecision_ThrowsProjectNotAvailableException_ForInvalidProjectStatus() {
        acceptDecisionDto = new ProcessProposalDecisionRequestDTO(ProposalDecisionAction.ACCEPT, "clientPassword");
        project.setStatus(ProjectStatus.IN_PROGRESS);

        when(proposalAccessHelper.findProposalById(proposalId)).thenReturn(proposal);
        doNothing().when(userAccessValidator).validateAccess(clientId);

        assertThrows(ProjectNotAvailableException.class, () -> proposalService.processProposalDecision(proposalId, acceptDecisionDto));
        verify(passwordValidator, never()).validate(anyString(), anyString(), anyString());
    }
}