package com.manager.freelancer_management_api.infra.handler;

import com.manager.freelancer_management_api.domain.dto.ApiResponseDTO;
import com.manager.freelancer_management_api.domain.dto.DTOValidationErrorResponse;
import com.manager.freelancer_management_api.domain.exceptions.UnauthorizedAccessException;
import com.manager.freelancer_management_api.domain.user.exceptions.*;
import com.manager.freelancer_management_api.utils.handler.ApiResponseUtil;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.manager.freelancer_management_api.utils.handler.ApiResponseUtil.buildErrorResponse;

@ControllerAdvice
@Import(ApiResponseUtil.class)
public class UserExceptionHandler {
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password.";

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponseDTO> unauthorizedAccessExceptionHandler(UnauthorizedAccessException e){
        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDTO> userNotFoundHandler(UserNotFoundException e){
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDTO> emailAlreadyExistsHandler(EmailAlreadyExistsException e){
        return buildErrorResponse(HttpStatus.CONFLICT, "Invalid email address.");
    }

    @ExceptionHandler(DocumentAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDTO> documentAlreadyExistsHandler(DocumentAlreadyExistsException e){
        return buildErrorResponse(HttpStatus.CONFLICT, "Invalid document.");
    }

    @ExceptionHandler(InvalidUserRoleException.class)
    public ResponseEntity<ApiResponseDTO> invalidUserRoleHandler(InvalidUserRoleException e){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<ApiResponseDTO> samePasswordHandler(SamePasswordException e){
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ApiResponseDTO> invalidEmailHandler(InvalidEmailException e){
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponseDTO> invalidPasswordHandler(InvalidPasswordException e){
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
}