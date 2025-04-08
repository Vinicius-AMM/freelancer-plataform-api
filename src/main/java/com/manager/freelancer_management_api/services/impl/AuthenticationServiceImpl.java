package com.manager.freelancer_management_api.services.impl;

import com.manager.freelancer_management_api.domain.repositories.UserRepository;
import com.manager.freelancer_management_api.domain.user.dto.request.LoginRequestDTO;
import com.manager.freelancer_management_api.domain.user.dto.request.RegisterUserRequestDTO;
import com.manager.freelancer_management_api.domain.user.dto.response.LoginResponseDTO;
import com.manager.freelancer_management_api.domain.user.entities.User;
import com.manager.freelancer_management_api.domain.user.exceptions.EmailAlreadyExistsException;
import com.manager.freelancer_management_api.domain.user.exceptions.InvalidEmailException;
import com.manager.freelancer_management_api.infra.security.TokenService;
import com.manager.freelancer_management_api.services.IAuthenticationService;
import com.manager.freelancer_management_api.utils.validator.PasswordValidator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationServiceImpl(UserRepository userRepository, PasswordValidator passwordValidator, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordValidator = passwordValidator;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO login) {
        var user = userRepository.findByEmail(login.email());

        if(user == null){
            throw new InvalidEmailException("Invalid email address.");
        }

        passwordValidator.validate(login.password(), user.getPassword(), "Invalid email or password.");

        String token = tokenService.generateToken(login);
        return new LoginResponseDTO(token);
    }

    @Override
    @Transactional
    public void register(RegisterUserRequestDTO registerData){
        if(userRepository.existsByEmail(registerData.email())){
            throw new EmailAlreadyExistsException();
        }
        String encryptedPassword = passwordEncoder.encode(registerData.password());
        User user = User.builder()
                .fullName(registerData.fullName())
                .document(registerData.document())
                .email(registerData.email())
                .password(encryptedPassword)
                .mainUserRole(registerData.mainUserRole())
                .currentUserRole(registerData.currentUserRole())
                .build();
        userRepository.save(user);
    }
}
