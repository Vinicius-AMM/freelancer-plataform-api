package com.manager.freelancer_management_api.utils.validator;

import com.manager.freelancer_management_api.domain.exceptions.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserAccessValidator {

    public void validateAccess(UUID userId){
        UUID foundUserId = getAuthenticatedUserId();

        if(!userId.equals(foundUserId)){
            throw new UnauthorizedAccessException();
        }
    }

    public UUID getAuthenticatedUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException("Access denied.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            String userIdString = jwt.getSubject();

            if (userIdString == null || userIdString.isBlank()) {
                throw new UnauthorizedAccessException("Token de autenticação inválido: Identificador de usuário ausente.");
            }

            try {
                return UUID.fromString(userIdString);
            } catch (IllegalArgumentException e) {
                throw new UnauthorizedAccessException("Token de autenticação inválido: Identificador de usuário malformado.");
            }
        } else {
            throw new UnauthorizedAccessException("Tipo de autenticação não suportado ou inválido.");
        }
    }
}