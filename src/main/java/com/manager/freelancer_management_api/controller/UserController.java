package com.manager.freelancer_management_api.controller;

import com.manager.freelancer_management_api.domain.dto.ApiResponseDTO;
import com.manager.freelancer_management_api.domain.dto.DTOValidationErrorResponse;
import com.manager.freelancer_management_api.domain.user.dto.request.*;
import com.manager.freelancer_management_api.domain.user.dto.response.OtherUserProfileDTO;
import com.manager.freelancer_management_api.domain.user.dto.response.UserProfileDTO;
import com.manager.freelancer_management_api.domain.user.dto.response.UserProfileResponseDTO;
import com.manager.freelancer_management_api.infra.security.SecurityConfig;
import com.manager.freelancer_management_api.services.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.manager.freelancer_management_api.utils.handler.ApiResponseUtil.buildSuccessResponse;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = SecurityConfig.SECURITY)
@Tag(name = "User Management", description = "Endpoints para operações de usuário")
public class UserController {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/profile")
    @Operation(summary = "Busca o perfil de usuário", description = "Retorna o perfil com propriedades diferentes caso seja o perfil próprio ou perfil de outro usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil do usuário autenticado encontrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(oneOf = {UserProfileDTO.class, OtherUserProfileDTO.class}),
                            examples = {@ExampleObject(
                                    name = "Own user profile",
                                    value = "{\"fullName\": \"Full Name\", \"email\": \"test@test.com\", \"document\": \"12345678901\", \"mainUserRole\": \"FREELANCER\", \"currentUserRole\": \"CLIENT\"}"),
                                    @ExampleObject(
                                            name = "Other user profile",
                                            value = "{\"fullName\": \"Full Name\", \"mainUserRole\": \"FREELANCER\", \"currentUserRole\": \"CLIENT\"}"
                                    )}
                    )),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"message\": \"User not found\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")))
    })
    public ResponseEntity<UserProfileResponseDTO> getUserProfile(@PathVariable UUID id) {
        UserProfileResponseDTO profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/{id}/updateFullName")
    @Operation(summary = "Altera o nome completo do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nome completo atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 200, \"message\": \"Full name updated successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
                    )),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 401, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
                    )),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (falha na validação do DTO)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DTOValidationErrorResponse.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"errors\": {\"newFullName\": \"Full name cannot be empty.\"}, \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "Não autorizado (ao tentar atualizar dados de outro usuário)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 403, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 404, \"message\": \"User not found\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
                    ))
    })
    public ResponseEntity<ApiResponseDTO> updateFullName(@PathVariable UUID id, @RequestBody @Valid FullNameUpdateRequestDTO request) {
        userService.updateFullName(id, request.newFullName());
        return buildSuccessResponse(HttpStatus.OK, "Full name updated successfully");
    }

    @PatchMapping("/{id}/updateEmail")
    @Operation(summary = "Atualiza o e-mail do usuário", description = "Permite que o usuário autenticado atualize seu próprio e-mail, requerendo a senha atual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 200, \"message\": \"Email updated successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (falha na validação do DTO)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DTOValidationErrorResponse.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"errors\": {\"email\": \"Invalid email format\", \"password\": \"Fill in this field\"}, \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (senha incorretos)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 401, \"message\": \"Invalid password.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "403", description = "Não autorizado (tentando atualizar dados de outro usuário)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 403, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 404, \"message\": \"User not found\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "409", description = "Endereço de e-mail inválido ou já cadastrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 409, \"message\": \"Invalid email address.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")))
    })
    public ResponseEntity<ApiResponseDTO> updateEmail(@PathVariable UUID id, @RequestBody @Valid EmailUpdateRequestDTO request) {
        userService.updateEmail(id, request.password(), request.email());
        return buildSuccessResponse(HttpStatus.OK, "Email updated successfully");
    }

    @PatchMapping("/{id}/updateDocument")
    @Operation(summary = "Atualiza o documento do usuário", description = "Permite que o usuário autenticado atualize seu próprio documento (CPF/CNPJ), requerendo a senha atual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 200, \"message\": \"Document updated successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (falha na validação do DTO).",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DTOValidationErrorResponse.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"errors\": {\"newDocument\": \"New document cannot be empty.\"}, \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (senha incorreta)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 401, \"message\": \"Invalid password.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "403", description = "Não autorizado (tentando atualizar dados de outro usuário)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 403, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 404, \"message\": \"User not found\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")))
    })
    public ResponseEntity<ApiResponseDTO> updateDocument(@PathVariable UUID id, @RequestBody @Valid DocumentUpdateRequestDTO request) {
        userService.updateDocument(id, request.password(), request.newDocument());
        return buildSuccessResponse(HttpStatus.OK, "Document updated successfully");
    }

    @PatchMapping("/{id}/updatePassword")
    @Operation(summary = "Atualiza a senha do usuário", description = "Permite que o usuário autenticado atualize sua própria senha, requerendo a senha antiga.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 200, \"message\": \"Password updated successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (falha na validação do DTO)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DTOValidationErrorResponse.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"errors\": {\"newPassword\": \"The password must have at least 6 characters.\"}, \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (email ou senha incorretos)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 401, \"message\": \"Invalid password.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "403", description = "Não autorizado (tentando atualizar dados de outro usuário)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 403, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 404, \"message\": \"User not found\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "409", description = "Nova senha não pode ser igual à antiga.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 409, \"message\": \"Passwords must not be the same.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")))
    })
    public ResponseEntity<ApiResponseDTO> updatePassword(@PathVariable UUID id, @RequestBody @Valid PasswordUpdateRequestDTO request) {
        userService.updatePassword(id, request.oldPassword(), request.newPassword());
        return buildSuccessResponse(HttpStatus.OK, "Password updated successfully");
    }

    @PatchMapping("/{id}/changeUserRole")
    @Operation(summary = "Altera o papel atual do usuário", description = "Permite que o usuário autenticado altere sua função atual na plataforma (ex: CLIENTE para FREELANCER).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Função atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 200, \"message\": \"Role updated successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (falha na validação do DTO) ou função de usuário inválida.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"message\": \"Invalid role. Valid roles are CLIENT or FREELANCER.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "403", description = "Não autorizado (tentando atualizar dados de outro usuário)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 403, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 404, \"message\": \"User not found\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
    })
    public ResponseEntity<ApiResponseDTO> changeUserRole(@PathVariable UUID id, @RequestBody @Valid UserRoleUpdateRequestDTO request) {
        userService.changeUserRole(id, request.newUserRole());
        return buildSuccessResponse(HttpStatus.OK, "User Role updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui a conta do usuário", description = "Permite que o usuário autenticado exclua apenas sua própria conta, requerendo a senha atual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário excluído com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 200, \"message\": \"User deleted successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (falha na validação do DTO)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DTOValidationErrorResponse.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"errors\": {\"password\": \"Password must not be null.\"}, \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (senha inválida)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 401, \"message\": \"Invalid password. \", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "403", description = "Não autorizado (tentando excluir outro usuário)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 403, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<ApiResponseDTO> deleteUser(@PathVariable UUID id, @RequestBody @Valid DeleteUserRequestDTO request) {
        userService.deleteById(id, request.password());
        return buildSuccessResponse(HttpStatus.OK, "User deleted successfully");
    }
}