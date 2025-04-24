package com.manager.freelancer_management_api.domain.user.service;

import com.manager.freelancer_management_api.domain.user.dto.request.LoginRequestDTO;
import com.manager.freelancer_management_api.domain.user.dto.request.RegisterUserRequestDTO;
import com.manager.freelancer_management_api.domain.user.dto.response.LoginResponseDTO;

public interface IAuthenticationService {
    LoginResponseDTO login(LoginRequestDTO login);
    void register(RegisterUserRequestDTO registerData);
}