package com.manager.freelancer_management_api.utils.security;

import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthConverter.class);

    private final UserRepository userRepository;

    public JwtAuthConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt){
        try {
            String userIdString = jwt.getSubject();
            if (userIdString == null) {
                log.warn("JWT subject (user ID) is null.");
                return new JwtAuthenticationToken(jwt, Collections.emptyList());
            }

            UUID userId = UUID.fromString(userIdString);
            User user = userRepository.findById(userId).orElse(null);

            Collection<GrantedAuthority> authorities = Collections.emptyList();
            if (user != null) {
                authorities = (Collection<GrantedAuthority>) user.getAuthorities();
                log.debug("Authorities carregadas para {}: {}", userId, authorities);
            } else {
                log.warn("User not found in DB for valid JWT subject: {}", userId);
            }

            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in JWT subject: {}", jwt.getSubject());
            return new JwtAuthenticationToken(jwt, Collections.emptyList());
        } catch (Exception e) {
            log.error("Failed to convert JWT to AuthenticationToken: {}", e.getMessage());
            return new JwtAuthenticationToken(jwt, Collections.emptyList());
        }
    }
}