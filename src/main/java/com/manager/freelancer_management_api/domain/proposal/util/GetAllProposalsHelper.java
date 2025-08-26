package com.manager.freelancer_management_api.domain.proposal.util;

import com.manager.freelancer_management_api.domain.proposal.dto.response.ProposalResponseDTO;
import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import com.manager.freelancer_management_api.utils.common.PaginationHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GetAllProposalsHelper {
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private final PaginationHelper paginationHelper;

    public GetAllProposalsHelper(PaginationHelper paginationHelper) {
        this.paginationHelper = paginationHelper;
    }

    public Page<ProposalResponseDTO> getAllProposals(Pageable pageable, Function<Pageable, Page<Proposal>> fetchFunction){
        Pageable effectivePageable = paginationHelper.preparePageRequest(pageable, DEFAULT_SORT_FIELD);

        Page<Proposal> proposalPage = fetchFunction.apply(effectivePageable);

        List<ProposalResponseDTO> list = proposalPage.getContent().stream()
                .map(ProposalResponseDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(list, proposalPage.getPageable(), proposalPage.getTotalElements());
    }
}
