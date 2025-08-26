package com.manager.freelancer_management_api.domain.proposal.exception;

public class ProposalNotFoundException extends RuntimeException {
    public ProposalNotFoundException(String message) {
        super(message);
    }
    public ProposalNotFoundException(){
        super("Proposal not found.");
    }

}
