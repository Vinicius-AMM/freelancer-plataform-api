package com.manager.freelancer_management_api.infra.security;

import com.manager.freelancer_management_api.domain.exceptions.InvalidTokenException;
import com.manager.freelancer_management_api.domain.repositories.UserRepository;
import com.manager.freelancer_management_api.domain.user.dto.request.LoginRequestDTO;
import com.manager.freelancer_management_api.domain.user.entities.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public TokenService(UserRepository userRepository, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String  generateToken(LoginRequestDTO login){
        User user = (User) userRepository.findByEmail(login.email());

        var now = Instant.now();
        var expirationTime = 7200L;

        var claims = JwtClaimsSet.builder()
                .issuer("Freelancer management api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationTime))
                .subject(String.valueOf(user.getId()))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
    public String isTokenValid(String token){
        try{
            jwtDecoder.decode(token);
            return "Valid Token.";
        } catch (Exception e){
            throw new InvalidTokenException();
        }
    }
}