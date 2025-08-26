package com.manager.freelancer_management_api.domain.proposal.util;

import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import com.manager.freelancer_management_api.domain.proposal.exception.ProposalNotFoundException;
import com.manager.freelancer_management_api.domain.proposal.repository.ProposalRepository;
import com.manager.freelancer_management_api.utils.validator.UserAccessValidator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProposalAccessHelper {
    private final ProposalRepository proposalRepository;
    private final UserAccessValidator userAccessValidator;

    public ProposalAccessHelper(ProposalRepository proposalRepository, UserAccessValidator userAccessValidator) {
        this.proposalRepository = proposalRepository;
        this.userAccessValidator = userAccessValidator;
    }

    public Proposal findProposalAndValidateOwnership(Long proposalId) {
        Proposal proposal = findProposalById(proposalId);

        UUID freelancerId = proposal.getFreelancer().getId();
        userAccessValidator.validateAccess(freelancerId);

        return proposal;
    }

    public Proposal findProposalById(Long proposalId){
        return proposalRepository.findById(proposalId).orElseThrow(ProposalNotFoundException::new);
    }
}