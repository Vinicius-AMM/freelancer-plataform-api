package com.manager.freelancer_management_api.domain.user.enums;

public enum UserRole {
    FREELANCER("freelancer"),
    CLIENT("client");

    private String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
