package com.manager.freelancer_management_api.utils.validator;

import com.manager.freelancer_management_api.domain.user.exceptions.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordValidator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void validate(String rawPassword, String encodedPassword, String errorMessage){
        if(!passwordEncoder.matches(rawPassword, encodedPassword)){
            throw new InvalidPasswordException(errorMessage);
        }
    }
}