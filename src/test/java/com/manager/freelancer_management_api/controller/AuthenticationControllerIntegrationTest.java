package com.manager.freelancer_management_api.controller;

import com.manager.freelancer_management_api.config.AbstractIntegrationTest;
import com.manager.freelancer_management_api.domain.repositories.UserRepository;
import com.manager.freelancer_management_api.domain.user.dto.request.LoginRequestDTO;
import com.manager.freelancer_management_api.domain.user.dto.request.RegisterUserRequestDTO;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String REGISTER_URL = "/auth/register";
    private final String LOGIN_URL = "/auth/login";

    @BeforeEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /auth/register - Should register user successfully")
    @Rollback
    void register_shouldCreateUserAndReturnCreated() throws Exception {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Test User",
                "12345678901",
                "test@example.com",
                "password",
                UserRole.FREELANCER,
                null
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.message", is("Successful registration.")));

        assertTrue(userRepository.existsByEmail("test@example.com"));
    }

    @Test
    @DisplayName("POST /auth/register - Should return Conflict when email already exists")
    void register_shouldReturnConflict_whenEmailExists() throws Exception {
        RegisterUserRequestDTO firstRequest = new RegisterUserRequestDTO(
                "Existing User", "55566677788", "conflict@example.com", "password123", UserRole.CLIENT, null);
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        RegisterUserRequestDTO secondRequest = new RegisterUserRequestDTO(
                "Another User", "99988877766", "conflict@example.com", "password456", UserRole.FREELANCER, null);
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode", is(409)))
                .andExpect(jsonPath("$.message", containsString("Invalid email address.")));
    }

    @Test
    @DisplayName("POST /auth/login - Should return token for valid credentials")
    void login_shouldReturnToken_forValidCredentials() throws Exception {
        String email = "test@example.com";
        String password = "password";
        RegisterUserRequestDTO registerRequest = new RegisterUserRequestDTO(
                "Login Test User", "12312312312", email, password, UserRole.FREELANCER, UserRole.CLIENT);
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(not(emptyString()))));
    }

    @Test
    @DisplayName("POST /auth/login - Should return Unauthorized for invalid password")
    void login_shouldReturnUnauthorized_forInvalidPassword() throws Exception {
        String email = "test@example.com";
        String password = "correctPassword";
        RegisterUserRequestDTO registerRequest = new RegisterUserRequestDTO(
                "Wrong Pass User", "45645645645", email, password, UserRole.CLIENT, null);
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginRequest = new LoginRequestDTO(email, "wrongPassword");
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode", is(401)))
                .andExpect(jsonPath("$.message", containsString("Invalid email or password.")));
    }

    @Test
    @DisplayName("POST /auth/login - Should return Unauthorized for non-existent user")
    void login_shouldReturnUnauthorized_forNonExistentUser() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode", is(401)))
                .andExpect(jsonPath("$.message", containsString("Invalid email or password.")));

        assertFalse(userRepository.existsByEmail("test@example.com"));
    }
}
