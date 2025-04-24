package com.manager.freelancer_management_api.domain.project.enums;

import lombok.Getter;

@Getter
public enum ProjectStatus {
    OPEN("open"),
    NEGOTIATING("negotiating"),
    IN_PROGRESS("in_progress"),
    FINISHED("finished"),
    CANCELLED("cancelled");

    private String status;

    ProjectStatus(String status) {
        this.status = status;
    }
}
