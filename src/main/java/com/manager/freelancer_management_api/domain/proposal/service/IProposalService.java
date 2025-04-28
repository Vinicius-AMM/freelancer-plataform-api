package com.manager.freelancer_management_api.domain.proposal.service;

import com.manager.freelancer_management_api.domain.proposal.dto.request.ProcessProposalDecisionRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.request.UpdateProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.request.CreateProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.response.ProposalResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IProposalService {
    void createProposal(Long projectId, CreateProposalRequestDTO proposalData);
    Page<ProposalResponseDTO> getAllProposalsByProjectId(Long projectId, Pageable pageable);
    Page<ProposalResponseDTO> getAllProposalsByFreelancerId(UUID freelancerId, Pageable pageable);
    ProposalResponseDTO getProposalById(Long proposalId);
    void updateProposal(Long proposalId, String password, UpdateProposalRequestDTO proposalData);
    void deleteProposal(Long proposalId, String password);
    void processProposalDecision(Long proposalId, ProcessProposalDecisionRequestDTO decisionRequest);
}