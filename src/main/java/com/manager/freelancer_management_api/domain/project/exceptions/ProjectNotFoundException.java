package com.manager.freelancer_management_api.domain.project.exceptions;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String message) {
        super(message);
    }

    public ProjectNotFoundException() {
        super("Project not found.");
    }
}
