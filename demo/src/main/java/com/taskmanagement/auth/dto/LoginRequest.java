package com.taskmanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for user login")
public record LoginRequest(

        @Schema(description = "Registered email address", example = "john.doe@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email name cannot be blank")
        @Email(message = "Email should be in a valid format")
        String email,

        @Schema(description = "Account password", example = "SecurePass123!",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password cannot be blank")
        String password

) {
}
