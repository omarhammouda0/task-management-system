package com.taskmanagement.auth.dto;



import com.taskmanagement.user.dto.UserResponseDto;

public record AuthResponse(

        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn

) {
    public AuthResponse {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
        if (expiresIn == null) {
            expiresIn = 900000L;
        }
    }

    public AuthResponse(String accessToken, String refreshToken, UserResponseDto user) {
        this(accessToken, refreshToken, "Bearer", 900000L);
    }
}