package com.manager.freelancer_management_api.domain.proposal.util;

import com.manager.freelancer_management_api.domain.global.exceptions.UnauthorizedAccessException;
import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import com.manager.freelancer_management_api.domain.proposal.exception.ProposalNotFoundException;
import com.manager.freelancer_management_api.domain.proposal.repository.ProposalRepository;
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
class ProposalAccessHelperTest {
    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private UserAccessValidator userAccessValidator;

    @InjectMocks
    private ProposalAccessHelper proposalAccessHelper;

    private Proposal proposal;
    private User freelancer;
    private Long proposalId;
    private UUID freelancerId;

    @BeforeEach
    void setUp() {
        proposalId = 10L;
        freelancerId = UUID.randomUUID();

        freelancer = User.builder().id(freelancerId).build();
        proposal = Proposal.builder().id(proposalId).freelancer(freelancer).build();
    }

    @Test
    @DisplayName("findProposalById should return proposal when found")
    void findProposalById_shouldReturnProposal_whenFound() {
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));

        Proposal foundProposal = proposalAccessHelper.findProposalById(proposalId);

        assertNotNull(foundProposal);
        assertEquals(proposalId, foundProposal.getId());
        verify(proposalRepository).findById(proposalId);
    }

    @Test
    @DisplayName("findProposalById should throw ProposalNotFoundException when not found")
    void findProposalById_shouldThrowProposalNotFoundException_whenNotFound() {
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        assertThrows(ProposalNotFoundException.class, () -> proposalAccessHelper.findProposalById(proposalId));
        verify(proposalRepository).findById(proposalId);
    }

    @Test
    @DisplayName("findProposalAndValidateOwnership should return proposal when found and user is the proposal owner (freelancer)")
    void findProposalAndValidateOwnership_shouldReturnProposal_whenFoundAndUserIsOwner() {
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        doNothing().when(userAccessValidator).validateAccess(freelancerId);

        Proposal foundProposal = proposalAccessHelper.findProposalAndValidateOwnership(proposalId);

        assertNotNull(foundProposal);
        assertEquals(proposalId, foundProposal.getId());
        verify(proposalRepository).findById(proposalId);
        verify(userAccessValidator).validateAccess(freelancerId);
    }

    @Test
    @DisplayName("findProposalAndValidateOwnership should throw ProposalNotFoundException when not found")
    void findProposalAndValidateOwnership_shouldThrowProposalNotFoundException_whenProposalNotFound() {
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        assertThrows(ProposalNotFoundException.class, () -> proposalAccessHelper.findProposalAndValidateOwnership(proposalId));
        verify(proposalRepository).findById(proposalId);
        verify(userAccessValidator, never()).validateAccess(any(UUID.class));
    }

    @Test
    @DisplayName("findProposalAndValidateOwnership should throw UnauthorizedAccessException when user is not the proposal owner (freelancer)")
    void findProposalAndValidateOwnership_shouldThrowUnauthorizedAccessException_whenUserIsNotOwner() {
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        doThrow(new UnauthorizedAccessException("Access denied.")).when(userAccessValidator).validateAccess(freelancerId);

        assertThrows(UnauthorizedAccessException.class, () -> proposalAccessHelper.findProposalAndValidateOwnership(proposalId));
        verify(proposalRepository).findById(proposalId);
        verify(userAccessValidator).validateAccess(freelancerId);
    }
}