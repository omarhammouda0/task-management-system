package com.taskmanagement.auth.service;


import com.taskmanagement.auth.dto.AuthResponse;
import com.taskmanagement.auth.dto.LoginRequest;
import com.taskmanagement.auth.dto.RefreshTokenRequest;
import com.taskmanagement.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(String refreshToken);
}