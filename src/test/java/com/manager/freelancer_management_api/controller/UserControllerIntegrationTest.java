package com.manager.freelancer_management_api.controller;

import com.manager.freelancer_management_api.config.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import com.manager.freelancer_management_api.domain.repositories.UserRepository;
import com.manager.freelancer_management_api.domain.user.dto.request.*;
import com.manager.freelancer_management_api.domain.user.entities.User;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private UUID userId;
    private String userEmail = "usertest@example.com";
    private String userPassword = "password";
    private String userDocument = "12345678901";


    private String otherUserToken;
    private UUID otherUserId;
    private String otherUserEmail = "otheruser@example.com";
    private String otherUserPassword = "password123";
    private String otherUserDocument = "09876543212345";


    private String registerAndLogin(String fullName, String document, String email, String password, UserRole userRole, UserRole currentUserRole) throws Exception {
        RegisterUserRequestDTO registerReq = new RegisterUserRequestDTO(fullName, document, email, password, userRole, currentUserRole);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginReq = new LoginRequestDTO(email, password);
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        return JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");
    }

    @BeforeEach
    @Transactional
    void setupUsers() throws Exception {
        userRepository.deleteAll();

        userToken = registerAndLogin("User Test", userDocument, userEmail, userPassword, UserRole.FREELANCER, UserRole.FREELANCER);
        User user = (User) userRepository.findByEmail(userEmail);
        assertNotNull(user, "Primary user setup failed");
        userId = user.getId();


        otherUserToken = registerAndLogin("Other User", otherUserDocument, otherUserEmail, otherUserPassword, UserRole.CLIENT, UserRole.CLIENT);
        User otherUser = (User) userRepository.findByEmail(otherUserEmail);
        assertNotNull(otherUser, "Other user setup failed");
        otherUserId = otherUser.getId();
    }

    @Test
    @DisplayName("GET /api/users/{id}/profile - Should return own full profile")
    void getUserProfile_shouldReturnOwnProfile() throws Exception {
        mockMvc.perform(get("/api/users/{id}/profile", userId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("User Test")))
                .andExpect(jsonPath("$.email", is(userEmail)))
                .andExpect(jsonPath("$.document", is(userDocument)))
                .andExpect(jsonPath("$.mainUserRole", is("FREELANCER")))
                .andExpect(jsonPath("$.currentUserRole", is("FREELANCER")));
    }

    @Test
    @DisplayName("GET /api/users/{id}/profile - Should return limited profile for other user")
    void getUserProfile_shouldReturnOtherUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/{id}/profile", otherUserId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Other User")))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.document").doesNotExist())
                .andExpect(jsonPath("$.mainUserRole", is("CLIENT")))
                .andExpect(jsonPath("$.currentUserRole", is("CLIENT")));
    }

    @Test
    @DisplayName("GET /api/users/{id}/profile - Should return 404 for non-existent user")
    void getUserProfile_shouldReturnNotFound_forNonExistentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/users/{id}/profile", nonExistentId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    @DisplayName("GET /api/users/{id}/profile - Should return 401 Unauthorized without token")
    void getUserProfile_shouldReturnUnauthorized_withoutToken() throws Exception {
        mockMvc.perform(get("/api/users/{id}/profile", userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/updateFullName - Should update own full name")
    void updateFullName_shouldUpdateOwnName() throws Exception {
        FullNameUpdateRequestDTO request = new FullNameUpdateRequestDTO("Updated User Name");

        mockMvc.perform(patch("/api/users/{id}/updateFullName", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Full name updated successfully")));

        mockMvc.perform(get("/api/users/{id}/profile", userId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(jsonPath("$.fullName", is("Updated User Name")));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/updateFullName - Should return 403 Forbidden when updating other user")
    void updateFullName_shouldReturnForbidden_forOtherUser() throws Exception {
        FullNameUpdateRequestDTO request = new FullNameUpdateRequestDTO("Trying To Update Other");

        mockMvc.perform(patch("/api/users/{id}/updateFullName", otherUserId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Access denied")));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/updateEmail - Should update own email with correct password")
    void updateEmail_shouldUpdateOwnEmail_withCorrectPassword() throws Exception {
        String newEmail = "newemail" + userEmail;
        EmailUpdateRequestDTO request = new EmailUpdateRequestDTO(userPassword, newEmail);

        mockMvc.perform(patch("/api/users/{id}/updateEmail", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Email updated successfully")));

        LoginRequestDTO loginReq = new LoginRequestDTO(newEmail, userPassword);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/updateEmail - Should return 401 Unauthorized with incorrect password")
    void updateEmail_shouldReturnUnauthorized_withIncorrectPassword() throws Exception {
        String newEmail = "anothernewemail" + userEmail;
        EmailUpdateRequestDTO request = new EmailUpdateRequestDTO("wrongPassword", newEmail);

        mockMvc.perform(patch("/api/users/{id}/updateEmail", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid password.")));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/updateEmail - Should return 409 Conflict if email already exists")
    void updateEmail_shouldReturnConflict_whenEmailExists() throws Exception {
        EmailUpdateRequestDTO request = new EmailUpdateRequestDTO(userPassword, otherUserEmail);

        mockMvc.perform(patch("/api/users/{id}/updateEmail", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Invalid email address.")));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/updateDocument - Should update own document with correct password")
    void updateDocument_shouldUpdateOwnDocument_withCorrectPassword() throws Exception {
        String newDocument = "99988877711";
        DocumentUpdateRequestDTO request = new DocumentUpdateRequestDTO(userPassword, newDocument);

        mockMvc.perform(patch("/api/users/{id}/updateDocument", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Document updated successfully")));

        mockMvc.perform(get("/api/users/{id}/profile", userId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(jsonPath("$.document", is(newDocument)));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/updateDocument - Should return 401 Unauthorized with incorrect password")
    void updateDocument_shouldReturnUnauthorized_withIncorrectPassword() throws Exception {
        String newDocument = "99988877722";
        DocumentUpdateRequestDTO request = new DocumentUpdateRequestDTO("wrongPassword", newDocument);

        mockMvc.perform(patch("/api/users/{id}/updateDocument", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid password.")));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/updateDocument - Should return 409 Conflict if document already exists")
    void updateDocument_shouldReturnConflict_whenDocumentExists() throws Exception {
        DocumentUpdateRequestDTO request = new DocumentUpdateRequestDTO(userPassword, otherUserDocument);

        mockMvc.perform(patch("/api/users/{id}/updateDocument", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Invalid document.")));
    }


    @Test
    @DisplayName("PATCH /api/users/{id}/updatePassword - Should update password with correct old password")
    void updatePassword_shouldUpdatePassword_withCorrectOldPassword() throws Exception {
        String newPassword = "newPasswordStronger";
        PasswordUpdateRequestDTO request = new PasswordUpdateRequestDTO(userPassword, newPassword);

        mockMvc.perform(patch("/api/users/{id}/updatePassword", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Password updated successfully")));

        LoginRequestDTO loginReq = new LoginRequestDTO(userEmail, newPassword);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/updatePassword - Should return 401 Unauthorized with incorrect old password")
    void updatePassword_shouldReturnUnauthorized_withIncorrectOldPassword() throws Exception {
        String newPassword = "newPasswordAttempt";
        PasswordUpdateRequestDTO request = new PasswordUpdateRequestDTO("wrongOldPassword", newPassword);

        mockMvc.perform(patch("/api/users/{id}/updatePassword", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid password.")));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/updatePassword - Should return 409 Conflict if new password is same as old")
    void updatePassword_shouldReturnConflict_ifPasswordIsSame() throws Exception {
        PasswordUpdateRequestDTO request = new PasswordUpdateRequestDTO(userPassword, userPassword);

        mockMvc.perform(patch("/api/users/{id}/updatePassword", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Passwords must not be the same.")));
    }


    @Test
    @DisplayName("PATCH /api/users/{id}/changeUserRole - Should change current role successfully")
    void changeUserRole_shouldChangeRole() throws Exception {
        UserRoleUpdateRequestDTO request = new UserRoleUpdateRequestDTO(UserRole.CLIENT.name());

        mockMvc.perform(patch("/api/users/{id}/changeUserRole", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User Role updated successfully")));

        mockMvc.perform(get("/api/users/{id}/profile", userId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(jsonPath("$.currentUserRole", is("CLIENT")));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/changeUserRole - Should return 400 Bad Request for invalid role")
    void changeUserRole_shouldReturnBadRequest_forInvalidRole() throws Exception {
        UserRoleUpdateRequestDTO request = new UserRoleUpdateRequestDTO("INVALID_ROLE");

        mockMvc.perform(patch("/api/users/{id}/changeUserRole", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid role. Valid roles are CLIENT or FREELANCER.")));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should delete own user with correct password")
    void deleteUser_shouldDeleteOwnUser_withCorrectPassword() throws Exception {
        DeleteUserRequestDTO request = new DeleteUserRequestDTO(userPassword);

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User deleted successfully")));

        mockMvc.perform(get("/api/users/{id}/profile", userId)
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isNotFound());

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should return 401 Unauthorized with incorrect password")
    void deleteUser_shouldReturnUnauthorized_withIncorrectPassword() throws Exception {
        DeleteUserRequestDTO request = new DeleteUserRequestDTO("wrongPassword");

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid password.")));

        assertTrue(userRepository.existsById(userId));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should return 403 Forbidden when deleting other user")
    void deleteUser_shouldReturnForbidden_whenDeletingOtherUser() throws Exception {
        DeleteUserRequestDTO request = new DeleteUserRequestDTO(userPassword);

        mockMvc.perform(delete("/api/users/{id}", otherUserId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Access denied.")));

        assertTrue(userRepository.existsById(otherUserId));
    }
}
