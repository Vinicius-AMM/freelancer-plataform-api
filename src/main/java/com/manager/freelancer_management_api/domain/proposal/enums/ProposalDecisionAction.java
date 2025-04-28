package com.manager.freelancer_management_api.domain.proposal.enums;

public enum ProposalDecisionAction {
    ACCEPT("accept"),
    DECLINE("decline");

    private String action;

    ProposalDecisionAction(String action) {
        this.action = action;
    }
}
