package com.manager.freelancer_management_api.domain.user.dto.request;

import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "DTO para cadastro de um novo usuário.")
public record RegisterUserRequestDTO(
        @NotNull
        @NotBlank
        @Size(max = 100, message = "Full name must be at most 100 characters.")
        @Schema(example = "Full Name")
        String fullName,

        @NotNull
        @Pattern(regexp = "^[0-9]{11}|[0-9]{14}$", message = "Field must have just 11 or 14 characters.")
        @Schema(example = "12345678901")
        String document,

        @NotNull
        @Size(max = 100, message = "Email must be at most 100 characters.")
        @Email(message = "Enter a valid email.")
        @Schema(example = "test@test.com")
        String email,

        @NotNull
        @Size(min = 6, max = 100, message = "The password must have at least 6 characters.")
        @Schema(example = "password")
        String password,

        @NotNull(message = "Main user role cannot be null.")
        @Schema(example = "FREELANCER")
        UserRole mainUserRole,
        @Schema(example = "CLIENT")
        UserRole currentUserRole
) {
}
