package com.manager.freelancer_management_api.domain.user.service;

import com.manager.freelancer_management_api.domain.user.dto.response.UserProfileResponseDTO;
import com.manager.freelancer_management_api.domain.user.entity.User;

import java.util.UUID;

public interface IUserService {
    User getUser(UUID id);
    UserProfileResponseDTO getUserProfile(UUID id);
    void updateFullName(UUID id, String newName);
    void updateEmail(UUID id, String password, String newEmail);
    void updateDocument(UUID id, String password, String newDocument);
    void updatePassword(UUID id, String oldPassword, String newPassword);
    void changeUserRole(UUID id, String newUserRole);
    void deleteById(UUID id, String userPassword);
}