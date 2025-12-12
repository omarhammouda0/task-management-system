package com.taskmanagement.auth.dto;


import com.taskmanagement.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT tokens")
public record AuthResponse(

        @Schema(description = "JWT access token for API authentication (valid for 15 minutes)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "JWT refresh token for obtaining new access tokens (valid for 7 days)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,

        @Schema(description = "Token type (always 'Bearer')", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token expiration time in milliseconds", example = "900000")
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