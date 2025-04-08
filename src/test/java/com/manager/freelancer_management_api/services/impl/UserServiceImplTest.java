package com.manager.freelancer_management_api.services.impl;

import com.manager.freelancer_management_api.domain.repositories.UserRepository;
import com.manager.freelancer_management_api.domain.user.dto.response.OtherUserProfileDTO;
import com.manager.freelancer_management_api.domain.user.dto.response.UserProfileDTO;
import com.manager.freelancer_management_api.domain.user.dto.response.UserProfileResponseDTO;
import com.manager.freelancer_management_api.domain.user.entities.User;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import com.manager.freelancer_management_api.domain.user.exceptions.*;
import com.manager.freelancer_management_api.utils.validator.PasswordValidator;
import com.manager.freelancer_management_api.utils.validator.UserAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private UserAccessValidator userAccessValidator;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UUID userId;

    private static final String INVALID_PASSWORD_MESSAGE = "Invalid password.";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .fullName("testUser")
                .document("12345678901")
                .email("test@email.com")
                .password("password")
                .currentUserRole(UserRole.CLIENT)
                .mainUserRole(UserRole.FREELANCER)
                .build();
    }

    @Test
    @DisplayName("getUser should return user")
    void testGetUser_ShouldReturnUser_Successfully(){
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(testUser));

        assertEquals(userService.getUser(userId), testUser);
        verify(userRepository, times(1)).findById(userId);
    }
    @Test
    @DisplayName("getUser should throw exception when user not found")
    void testGetUser_ShouldThrowException_UserNotFound(){
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUser(userId));

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("getUserProfile should return UserProfileDTO for authenticated user`s own profile")
    void testGetUserProfile_ShouldReturnUserProfileDTO() {
        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserProfileResponseDTO profile = userService.getUserProfile(userId);

        assertNotNull(profile);
        assertInstanceOf(UserProfileDTO.class, profile);
        UserProfileDTO userProfile = (UserProfileDTO) profile;
        assertEquals(userProfile.fullName(), testUser.getFullName());
        assertEquals(userProfile.email(), testUser.getEmail());

        verify(userRepository, times(1)).findById(userId);
    }
    @Test
    @DisplayName("getUserProfile should return OtherUserProfileDTO for another user`s profile")
    void testGetUserProfile_ShouldReturnOtherUserProfileDTO(){
        UUID otherUserId = UUID.randomUUID();
        User otherUser = User.builder()
                .id(otherUserId).fullName("Other User")
                .mainUserRole(UserRole.FREELANCER)
                .currentUserRole(UserRole.CLIENT)
                .build();

        when(userAccessValidator.getAuthenticatedUserId()).thenReturn(userId);
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));

        UserProfileResponseDTO profile = userService.getUserProfile(otherUserId);

        assertNotNull(profile);
        assertInstanceOf(OtherUserProfileDTO.class, profile);
        OtherUserProfileDTO otherUserProfile = (OtherUserProfileDTO) profile;
        assertEquals(otherUserProfile.fullName(), otherUser.getFullName());

        verify(userAccessValidator, times(1)).getAuthenticatedUserId();
        verify(userRepository, times(1)).findById(otherUserId);
    }

    @Test
    @DisplayName("updateFullName should update name successfully")
    void testUpdateFullName_ShouldUpdateName_Successfully() {
        String newName = "New User Name";
        doNothing().when(userAccessValidator).validateAccess(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        userService.updateFullName(userId, newName);

        verify(userAccessValidator, times(1)).validateAccess(userId);
        verify(userRepository, times(1)).findById(userId);
        assertEquals(newName, testUser.getFullName());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("updateEmail should update email successfully")
    void testUpdateEmail_ShouldUpdateEmail_Successfully(){
        String newEmail = "newemail@test.com";
        String currentPassword = "password";
        doNothing().when(userAccessValidator).validateAccess(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(passwordValidator).validate(currentPassword, testUser.getPassword(), INVALID_PASSWORD_MESSAGE);
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);

        userService.updateEmail(userId, currentPassword, newEmail);

        verify(userAccessValidator).validateAccess(userId);
        verify(passwordValidator).validate(currentPassword, testUser.getPassword(), INVALID_PASSWORD_MESSAGE);
        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(newEmail);
    }
    @Test
    @DisplayName("updateEmail should throw EmailAlreadyExistsException when email exists")
    void testUpdateEmail_ShouldThrowEmailAlreadyExists_WhenEmailExists() {
        String existingEmail = "existing@test.com";
        String currentPassword = "password";
        doNothing().when(userAccessValidator).validateAccess(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(passwordValidator).validate(currentPassword, testUser.getPassword(), INVALID_PASSWORD_MESSAGE);
        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateEmail(userId, currentPassword, existingEmail));
        verify(userRepository).existsByEmail(existingEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateDocument should update document successfully")
    void testUpdateDocument_ShouldUpdateDocument_Successfully(){
        String newDocument = "12345678901234";
        String currentPassword = "password";
        doNothing().when(userAccessValidator).validateAccess(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(passwordValidator).validate(currentPassword, testUser.getPassword(), INVALID_PASSWORD_MESSAGE);
        when(userRepository.existsByDocument(newDocument)).thenReturn(false);

        userService.updateDocument(userId, currentPassword, newDocument);

        verify(userAccessValidator).validateAccess(userId);
        verify(passwordValidator).validate(currentPassword, testUser.getPassword(), INVALID_PASSWORD_MESSAGE);
        verify(userRepository).findById(userId);
        verify(userRepository).existsByDocument(newDocument);
    }
    @Test
    @DisplayName("updateDocument should throw DocumentAlreadyExistsException when document exists")
    void testUpdateDocument_ShouldThrowDocumentAlreadyExists_WhenDocumentExists(){
        String existingDocument = "12345678901";
        String currentPassword = "password";
        doNothing().when(userAccessValidator).validateAccess(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(passwordValidator).validate(currentPassword, testUser.getPassword(), INVALID_PASSWORD_MESSAGE);
        when(userRepository.existsByDocument(existingDocument)).thenReturn(true);

        assertThrows(DocumentAlreadyExistsException.class, () -> userService.updateDocument(userId, currentPassword, existingDocument));
        verify(userRepository).existsByDocument(existingDocument);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updatePassword should update password successfully")
    void updatePassword_shouldUpdatePasswordSuccessfully() {
        String oldPassword = "password";
        String newPassword = "newPassword";
        String encodedNewPassword = "encodedNewPassword";
        doNothing().when(userAccessValidator).validateAccess(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(passwordValidator).validate(oldPassword, testUser.getPassword(), INVALID_PASSWORD_MESSAGE);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        userService.updatePassword(userId, oldPassword, newPassword);

        verify(userAccessValidator).validateAccess(userId);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(argThat(user -> user.getPassword().equals(encodedNewPassword)));
    }
    @Test
    @DisplayName("updatePassword should throw SamePasswordException if passwords are equal")
    void updatePassword_shouldThrowSamePasswordException_ifPasswordsAreEqual() {
        String oldPassword = "password";
        String newPassword = "password";
        doNothing().when(userAccessValidator).validateAccess(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(passwordValidator).validate(oldPassword, testUser.getPassword(), INVALID_PASSWORD_MESSAGE);

        assertThrows(SamePasswordException.class, () -> {
            userService.updatePassword(userId, oldPassword, newPassword);
        });

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("changeUserRole should change role successfully")
    void changeUserRole_shouldChangeRole_Successfully() {
        String newRoleString = "CLIENT";
        UserRole newRole = UserRole.CLIENT;
        doNothing().when(userAccessValidator).validateAccess(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        userService.changeUserRole(userId, newRoleString);

        verify(userAccessValidator).validateAccess(userId);
        verify(userRepository).save(argThat(user -> user.getCurrentUserRole().equals(newRole)));
    }
    @Test
    @DisplayName("changeUserRole should throw InvalidUserRoleException for invalid role")
    void changeUserRole_shouldThrowInvalidUserRoleException_forInvalidUserRole() {
        String invalidUserRole = "INVALID";
        doNothing().when(userAccessValidator).validateAccess(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidUserRoleException.class, () -> {
            userService.changeUserRole(userId, invalidUserRole);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("deleteById should delete user successfully")
    void deleteById_shouldDeleteUser_Successfully() {
        String userPassword = "password";
        doNothing().when(userAccessValidator).validateAccess(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(passwordValidator).validate(userPassword, testUser.getPassword(), INVALID_PASSWORD_MESSAGE);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteById(userId, userPassword);

        verify(userAccessValidator).validateAccess(userId);
        verify(passwordValidator).validate(userPassword, testUser.getPassword(), INVALID_PASSWORD_MESSAGE);
        verify(userRepository).deleteById(userId);
    }

}