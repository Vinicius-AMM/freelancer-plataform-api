package com.manager.freelancer_management_api.utils.validator;

import com.manager.freelancer_management_api.domain.exceptions.UnauthorizedAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserAccessValidatorTest {

    private UserAccessValidator userAccessValidator;

    private Authentication authentication;
    private SecurityContext securityContext;
    private Jwt jwt;

    private UUID authenticatedUserId;
    private String authenticatedUserIdString;

    @BeforeEach
    void setUp() {
        userAccessValidator = new UserAccessValidator();

        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);
        jwt = mock(Jwt.class);

        authenticatedUserId = UUID.randomUUID();
        authenticatedUserIdString = authenticatedUserId.toString();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getAuthenticatedUserId should return correct UUID when authenticated with valid Jwt")
    void getAuthenticatedUserId_shouldReturnCorrectUuid_whenValidJwt() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(authenticatedUserIdString);

        UUID actualUserId = userAccessValidator.getAuthenticatedUserId();

        assertEquals(authenticatedUserId, actualUserId);
    }

    @Test
    @DisplayName("getAuthenticatedUserId should throw UnauthorizedAccessException when Authentication is null")
    void getAuthenticatedUserId_shouldThrowException_whenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            userAccessValidator.getAuthenticatedUserId();
        });
        assertEquals("Access denied.", exception.getMessage());
    }

    @Test
    @DisplayName("getAuthenticatedUserId should throw UnauthorizedAccessException when not authenticated")
    void getAuthenticatedUserId_shouldThrowException_whenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            userAccessValidator.getAuthenticatedUserId();
        });
        assertEquals("Access denied.", exception.getMessage());
    }

    @Test
    @DisplayName("getAuthenticatedUserId should throw UnauthorizedAccessException when principal is not Jwt")
    void getAuthenticatedUserId_shouldThrowException_whenPrincipalIsNotJwt() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("not a jwt principal");

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            userAccessValidator.getAuthenticatedUserId();
        });
        assertEquals("Tipo de autenticação não suportado ou inválido.", exception.getMessage());
    }

    @Test
    @DisplayName("getAuthenticatedUserId should throw UnauthorizedAccessException when Jwt subject is null")
    void getAuthenticatedUserId_shouldThrowException_whenJwtSubjectIsNull() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(null);

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            userAccessValidator.getAuthenticatedUserId();
        });
        assertEquals("Token de autenticação inválido: Identificador de usuário ausente.", exception.getMessage());
    }

    @Test
    @DisplayName("getAuthenticatedUserId should throw UnauthorizedAccessException when Jwt subject is blank")
    void getAuthenticatedUserId_shouldThrowException_whenJwtSubjectIsBlank() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("   ");

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            userAccessValidator.getAuthenticatedUserId();
        });
        assertEquals("Token de autenticação inválido: Identificador de usuário ausente.", exception.getMessage());
    }

    @Test
    @DisplayName("getAuthenticatedUserId should throw UnauthorizedAccessException when Jwt subject is not a valid UUID")
    void getAuthenticatedUserId_shouldThrowException_whenJwtSubjectIsNotValidUuid() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("not-a-valid-uuid");

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            userAccessValidator.getAuthenticatedUserId();
        });
        assertEquals("Token de autenticação inválido: Identificador de usuário malformado.", exception.getMessage());
    }

    @Test
    @DisplayName("validateAccess should not throw exception when userId matches authenticated user")
    void validateAccess_shouldNotThrowException_whenUserIdMatches() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(authenticatedUserIdString);

        assertDoesNotThrow(() -> {
            userAccessValidator.validateAccess(authenticatedUserId);
        });
    }

    @Test
    @DisplayName("validateAccess should throw UnauthorizedAccessException when userId does not match authenticated user")
    void validateAccess_shouldThrowException_whenUserIdDoesNotMatch() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(authenticatedUserIdString);

        UUID differentUserId = UUID.randomUUID();

        assertThrows(UnauthorizedAccessException.class, () -> {
            userAccessValidator.validateAccess(differentUserId);
        });
    }

    @Test
    @DisplayName("validateAccess should throw UnauthorizedAccessException when user is not authenticated")
    void validateAccess_shouldThrowException_whenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        UUID targetUserId = UUID.randomUUID();

        assertThrows(UnauthorizedAccessException.class, () -> {
            userAccessValidator.validateAccess(targetUserId);
        });
    }
}