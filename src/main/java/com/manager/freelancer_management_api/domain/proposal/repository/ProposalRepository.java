package com.manager.freelancer_management_api.domain.proposal.repository;

import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    Page<Proposal> findAllByProjectId(Long projectId, Pageable pageable);
    Page<Proposal> findAllByFreelancerId(UUID freelancerId, Pageable pageable);
}
