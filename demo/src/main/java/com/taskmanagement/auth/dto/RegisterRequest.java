package com.taskmanagement.auth.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request body for user registration")
public record RegisterRequest(

        @Schema(description = "Username for the account", example = "johndoe",
                requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 50)
        @NotBlank (message = "User name cannot be blank")
        @Size(min = 2, max = 50 , message = "Username must be between 2 and 50 characters")
        String userName,

        @Schema(description = "Email address (must be unique)", example = "john.doe@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank (message = "Email name cannot be blank")
        @Email (message = "Email should be in a valid format")
        String email,

        @Schema(description = "Password (min 10 chars, must include uppercase, lowercase, digit, and special character)",
                example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 10)
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$",
                message = "Password must be at least 10 characters long " +
                        "and contain at least one uppercase letter," +
                        " one lowercase letter," +
                        " one digit," +
                        " and one special character" )
        String password,

        @Schema(description = "User's first name", example = "John",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "First name cannot be blank")
        String firstName,

        @Schema(description = "User's last name", example = "Doe",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Last name cannot be blank")
        String lastName



) {
}


