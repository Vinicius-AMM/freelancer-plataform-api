package com.manager.freelancer_management_api.utils.validator;

import com.manager.freelancer_management_api.domain.user.exceptions.InvalidPasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordValidator passwordValidator;

    private String rawPassword;
    private String encodedPassword;
    private String errorMessage;

    @BeforeEach
    void setUp() {
        rawPassword = "plainPassword";
        encodedPassword = "encodedPassword123";
        errorMessage = "Senha inválida fornecida!";
    }

    @Test
    @DisplayName("validate should not throw exception when passwords match")
    void validate_shouldNotThrowException_whenPasswordsMatch() {
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        assertDoesNotThrow(() -> {
            passwordValidator.validate(rawPassword, encodedPassword, errorMessage);
        });
    }

    @Test
    @DisplayName("validate should throw InvalidPasswordException when passwords do not match")
    void validate_shouldThrowInvalidPasswordException_whenPasswordsDoNotMatch() {
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> {
            passwordValidator.validate(rawPassword, encodedPassword, errorMessage);
        });
    }

    @Test
    @DisplayName("validate should throw exception with the correct error message when passwords do not match")
    void validate_shouldThrowExceptionWithCorrectMessage_whenPasswordsDoNotMatch() {
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            passwordValidator.validate(rawPassword, encodedPassword, errorMessage);
        });

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @DisplayName("validate should handle null raw password correctly (delegating to encoder)")
    void validate_shouldHandleNullRawPassword() {
        when(passwordEncoder.matches(null, encodedPassword)).thenReturn(false);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            passwordValidator.validate(null, encodedPassword, errorMessage);
        });
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @DisplayName("validate should handle null encoded password correctly (delegating to encoder)")
    void validate_shouldHandleNullEncodedPassword() {
        when(passwordEncoder.matches(rawPassword, null)).thenReturn(false);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            passwordValidator.validate(rawPassword, null, errorMessage);
        });
        assertEquals(errorMessage, exception.getMessage());
    }
}