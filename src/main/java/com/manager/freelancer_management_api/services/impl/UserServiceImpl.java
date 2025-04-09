package com.manager.freelancer_management_api.services.impl;

import com.manager.freelancer_management_api.domain.repositories.UserRepository;
import com.manager.freelancer_management_api.domain.user.dto.response.OtherUserProfileDTO;
import com.manager.freelancer_management_api.domain.user.dto.response.UserProfileDTO;
import com.manager.freelancer_management_api.domain.user.dto.response.UserProfileResponseDTO;
import com.manager.freelancer_management_api.domain.user.entities.User;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import com.manager.freelancer_management_api.domain.user.exceptions.*;
import com.manager.freelancer_management_api.services.IUserService;
import com.manager.freelancer_management_api.utils.validator.PasswordValidator;
import com.manager.freelancer_management_api.utils.validator.UserAccessValidator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@EnableCaching
public class UserServiceImpl implements IUserService {
    public static final String INVALID_PASSWORD_MESSAGE = "Invalid password.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final UserAccessValidator userAccessValidator;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordValidator passwordValidator, UserAccessValidator userAccessValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
        this.userAccessValidator = userAccessValidator;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "userProfileCache", key = "#id")
    public UserProfileResponseDTO getUserProfile(UUID id) {
        UUID authenticatedUserId = userAccessValidator.getAuthenticatedUserId();

        if(authenticatedUserId.equals(id)){
            User user = getUser(authenticatedUserId);
            return new UserProfileDTO(user.getFullName(),
                    user.getEmail(),
                    user.getDocument(),
                    user.getMainUserRole().toString(),
                    user.getCurrentUserRole().toString());
        } else {
            User otherUser = getUser(id);
            return new OtherUserProfileDTO(otherUser.getFullName(),
                    otherUser.getMainUserRole().toString(),
                    otherUser.getCurrentUserRole().toString());
        }
    }

    @Override
    @CacheEvict(value = "userProfileCache", key = "#id")
    public void updateFullName(UUID id, String newFullName) {
        userAccessValidator.validateAccess(id);

        User user = this.getUser(id);
        user.setFullName(newFullName);
        userRepository.save(user);
    }

    @Override
    @CacheEvict(value = "userProfileCache", key = "#id")
    public void updateEmail(UUID id, String password, String newEmail) {
        userAccessValidator.validateAccess(id);

        User user = this.getUser(id);
        passwordValidator.validate(password, user.getPassword(), INVALID_PASSWORD_MESSAGE);
        if(userRepository.existsByEmail(newEmail)){
            throw new EmailAlreadyExistsException();
        }
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    @Override
    @CacheEvict(value = "userProfileCache", key = "#id")
    public void updateDocument(UUID id, String password, String newDocument) {
        userAccessValidator.validateAccess(id);

        User user = this.getUser(id);
        passwordValidator.validate(password, user.getPassword(), INVALID_PASSWORD_MESSAGE);
        if(userRepository.existsByDocument(newDocument)){
            throw new DocumentAlreadyExistsException();
        }
        user.setDocument(newDocument);
        userRepository.save(user);
    }

    @Override
    public void updatePassword(UUID id, String oldPassword, String newPassword) {
        userAccessValidator.validateAccess(id);

        User user = this.getUser(id);
        passwordValidator.validate(oldPassword, user.getPassword(), INVALID_PASSWORD_MESSAGE);
        if(oldPassword.equals(newPassword)){
            throw new SamePasswordException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @CacheEvict(value = "userProfileCache", key = "#id")
    public void changeUserRole(UUID id, String newUserRole) {
        userAccessValidator.validateAccess(id);

        User user = this.getUser(id);

        try{
            user.setCurrentUserRole(UserRole.valueOf(newUserRole.toUpperCase()));
            userRepository.save(user);
        } catch (IllegalArgumentException e){
            throw new InvalidUserRoleException();
        }
    }

    @Override
    @CacheEvict(value = "userProfileCache", key = "#id")
    public void deleteById(UUID id, String userPassword) {
        userAccessValidator.validateAccess(id);

        User user = this.getUser(id);
        passwordValidator.validate(userPassword, user.getPassword(), INVALID_PASSWORD_MESSAGE);
        userRepository.deleteById(id);
    }
}