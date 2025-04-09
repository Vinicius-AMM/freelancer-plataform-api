package com.manager.freelancer_management_api.controller;

import com.manager.freelancer_management_api.domain.dto.ApiResponseDTO;
import com.manager.freelancer_management_api.domain.dto.DTOValidationErrorResponse;
import com.manager.freelancer_management_api.domain.user.dto.request.LoginRequestDTO;
import com.manager.freelancer_management_api.domain.user.dto.request.RegisterUserRequestDTO;
import com.manager.freelancer_management_api.domain.user.dto.response.LoginResponseDTO;
import com.manager.freelancer_management_api.services.IAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.manager.freelancer_management_api.utils.handler.ApiResponseUtil.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication controller", description = "Endpoints para autenticação e registro do usuário")
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    public AuthenticationController(IAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Auntentica o usuário", description = "Valida as credenciais do usuário e retorna um token JWT em caso de sucesso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos (falha no formato ou validação)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DTOValidationErrorResponse.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"errors\": {\"email\": \"Invalid email format\", \"password\": \"Fill in this field\"}, \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (email ou senha incorretos)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 401, \"message\": \"Invalid email or password.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")))
    })
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginData){
        LoginResponseDTO token = authenticationService.login(loginData);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    @Operation(summary = "Registra o usuário", description = "Cria uma nova conta de usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 201, \"message\": \"Successful registration.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
                    )),
            @ApiResponse(responseCode = "400", description = "Dados de registro inválidos (falha na validação do DTO)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DTOValidationErrorResponse.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"errors\": {\"fullName\": \"must not be blank\", \"document\": \"Field must have just 11 or 14 characters.\", \"email\": \"Enter a valid email.\", \"password\": \"The password must have at least 6 characters.\", \"mainUserRole\": \"Main user role cannot be null.\"}, \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "409", description = "Endereço de e-mail inválido ou já cadastrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 409, \"message\": \"Invalid email address.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")))
    })
    public ResponseEntity<ApiResponseDTO> register(@RequestBody @Valid RegisterUserRequestDTO registrationData){
        authenticationService.register(registrationData);
        return buildSuccessResponse(HttpStatus.CREATED, "Successful registration.");
    }
}
