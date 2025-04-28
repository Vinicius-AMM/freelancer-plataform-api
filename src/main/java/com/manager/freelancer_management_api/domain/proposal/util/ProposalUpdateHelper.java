package com.manager.freelancer_management_api.domain.proposal.util;

import com.manager.freelancer_management_api.domain.proposal.dto.request.UpdateProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import com.manager.freelancer_management_api.utils.common.DeadlineUpdateUtil;
import org.springframework.stereotype.Component;

@Component
public class ProposalUpdateHelper {
    public ProposalUpdateHelper(){}

    public boolean updateOfferedValue(Proposal proposal, UpdateProposalRequestDTO updateData) {
        boolean updated = false;
        if(updateData.newOfferedValue() != null){
            proposal.setOfferedValue(updateData.newOfferedValue());
            updated = true;
        }
        return updated;
    }

    public boolean updateDeadlineIfNecessary(Proposal proposal, UpdateProposalRequestDTO updateData) {
        if (updateData.newStartDate() == null && updateData.newEndDate() == null) {
            return false;
        }
        DeadlineUpdateUtil.applyDeadlineUpdateLogic(
                proposal::getDeadline,
                proposal::setDeadline,
                updateData.newStartDate(),
                updateData.newEndDate());

        return true;
    }
}