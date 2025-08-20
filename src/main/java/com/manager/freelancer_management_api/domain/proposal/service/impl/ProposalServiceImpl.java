package com.manager.freelancer_management_api.domain.proposal.service.impl;

import com.manager.freelancer_management_api.domain.global.exceptions.UnauthorizedAccessException;
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
import com.manager.freelancer_management_api.domain.proposal.repository.ProposalRepository;
import com.manager.freelancer_management_api.domain.proposal.service.IProposalService;
import com.manager.freelancer_management_api.domain.proposal.util.GetAllProposalsHelper;
import com.manager.freelancer_management_api.domain.proposal.util.ProposalAccessHelper;
import com.manager.freelancer_management_api.domain.proposal.util.ProposalUpdateHelper;
import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import com.manager.freelancer_management_api.domain.user.exceptions.InvalidUserRoleException;
import com.manager.freelancer_management_api.domain.user.service.IUserService;
import com.manager.freelancer_management_api.utils.validator.PasswordValidator;
import com.manager.freelancer_management_api.utils.validator.UserAccessValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Function;

@Service
public class ProposalServiceImpl implements IProposalService {

    private final ProposalRepository proposalRepository;
    private final ProposalAccessHelper proposalAccessHelper;
    private final PasswordValidator passwordValidator;
    private final UserAccessValidator userAccessValidator;
    private final IUserService userService;
    private final ProjectAccessHelper projectAccessHelper;
    private final ProposalUpdateHelper proposalUpdateHelper;
    private final GetAllProposalsHelper getAllProposalsHelper;
    private final CacheManager cacheManager;
    private final ProjectRepository projectRepository;

    public ProposalServiceImpl(ProposalRepository proposalRepository, ProposalAccessHelper proposalAccessHelper, PasswordValidator passwordValidator, UserAccessValidator userAccessValidator, IUserService userService, ProjectAccessHelper projectAccessHelper, ProposalUpdateHelper proposalUpdateHelper, GetAllProposalsHelper getAllProposalsHelper, CacheManager cacheManager, ProjectRepository projectRepository) {
        this.proposalRepository = proposalRepository;
        this.proposalAccessHelper = proposalAccessHelper;
        this.passwordValidator = passwordValidator;
        this.userAccessValidator = userAccessValidator;
        this.userService = userService;
        this.projectAccessHelper = projectAccessHelper;
        this.proposalUpdateHelper = proposalUpdateHelper;
        this.getAllProposalsHelper = getAllProposalsHelper;
        this.cacheManager = cacheManager;
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('FREELANCER')")
    @CacheEvict(value = "getProjectCache", key = "#projectId")
    public void createProposal(Long projectId, CreateProposalRequestDTO proposalData) {
        Project project = projectAccessHelper.findProjectById(projectId);

        UUID authenticatedFreelancerId = userAccessValidator.getAuthenticatedUserId();
        User freelancer = userService.getUser(authenticatedFreelancerId);
        if(freelancer.getCurrentUserRole() != UserRole.FREELANCER){
            throw new InvalidUserRoleException("Only freelancers can create proposals.");
        }
        if(project.getStatus() != ProjectStatus.OPEN && project.getStatus() != ProjectStatus.NEGOTIATING){
            throw new ProjectNotAvailableException("Proposals can only be created for projects that are OPEN or NEGOTIATING. Current status: " + project.getStatus());
        }

        Proposal proposal = proposalData.toEntity(proposalData, project, freelancer);

        project.setStatus(ProjectStatus.NEGOTIATING);
        proposalRepository.save(proposal);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    public Page<ProposalResponseDTO> getAllProposalsByProjectId(Long projectId, Pageable pageable) {
        Project project = projectAccessHelper.findProjectById(projectId);
        userAccessValidator.validateAccess(project.getUser().getId());

        Function<Pageable, Page<Proposal>> fetcher = effectivePageable ->
                proposalRepository.findAllByProjectId(projectId, effectivePageable);

        return getAllProposalsHelper.getAllProposals(pageable, fetcher);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('FREELANCER')")
    public Page<ProposalResponseDTO> getAllProposalsByFreelancerId(UUID freelancerId, Pageable pageable) {
        userAccessValidator.validateAccess(freelancerId);

        Function<Pageable, Page<Proposal>> fetcher = effectivePageable ->
                proposalRepository.findAllByFreelancerId(freelancerId, effectivePageable);

        return getAllProposalsHelper.getAllProposals(pageable, fetcher);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ProposalResponseDTO getProposalById(Long proposalId) {
        Proposal proposal = proposalAccessHelper.findProposalById(proposalId);
        try{
            userAccessValidator.validateAccess(proposal.getFreelancer().getId());
        } catch (UnauthorizedAccessException eFreelancer){
            try {
                userAccessValidator.validateAccess(proposal.getProject().getUser().getId());
            } catch (UnauthorizedAccessException eClient) {
                throw new UnauthorizedAccessException("You don't have access to this proposal.");
            }
        }
        return new ProposalResponseDTO(proposal);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('FREELANCER')")
    public void updateProposal(Long proposalId, UpdateProposalRequestDTO proposalData) {
        Proposal proposal = proposalAccessHelper.findProposalAndValidateOwnership(proposalId);

        boolean updated = proposalUpdateHelper.updateOfferedValue(proposal, proposalData);
        updated |= proposalUpdateHelper.updateDeadlineIfNecessary(proposal, proposalData);

        if (updated) {
            proposalRepository.save(proposal);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('FREELANCER')")
    public void deleteProposal(Long proposalId, String rawPassword) {
        Proposal proposal = proposalAccessHelper.findProposalAndValidateOwnership(proposalId);

        passwordValidator.validate(rawPassword,
                proposal.getFreelancer().getPassword(),
                "Invalid password. It was not possible to delete the proposal.");

        proposalRepository.delete(proposal);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    public void processProposalDecision(Long proposalId, ProcessProposalDecisionRequestDTO decisionRequest) {
        Proposal proposal = proposalAccessHelper.findProposalById(proposalId);
        Project project = proposal.getProject();
        User client = project.getUser();
        User freelancer = proposal.getFreelancer();

        userAccessValidator.validateAccess(client.getId());

        if(project.getStatus() != ProjectStatus.OPEN && project.getStatus() != ProjectStatus.NEGOTIATING){
            throw new ProjectNotAvailableException("Proposals can only be accepted or rejected for projects that are OPEN or NEGOTIATING. Current status: " + project.getStatus());
        }

        passwordValidator.validate(decisionRequest.rawPassword(),
                client.getPassword(),
                "Invalid password. It was not possible to process the proposal decision."
        );

        ProposalDecisionAction action = decisionRequest.action();

        if(action == ProposalDecisionAction.ACCEPT){
            project.setStatus(ProjectStatus.IN_PROGRESS);
            project.setAcceptedFreelancer(freelancer);
            project.setEstimatedBudget(proposal.getOfferedValue());
            projectRepository.save(project);
            cacheManager.getCache("getProjectCache").evict(project.getId());
        }

        if(action == ProposalDecisionAction.DECLINE) {
            proposalRepository.delete(proposal);
            if(project.getStatus() == ProjectStatus.NEGOTIATING && proposalRepository.countByProjectId(project.getId()) == 0) {
                project.setStatus(ProjectStatus.OPEN);
                projectRepository.save(project);
                cacheManager.getCache("getProjectCache").evict(project.getId());
            }
        }
    }
}