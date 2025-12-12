package com.taskmanagement.user.dto;

import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request body for updating a user. All fields are optional - only provided fields will be updated.")
public record UserUpdateDto(

        @Schema(description = "New email address", example = "updated.email@example.com",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 255)
        @Email(message = "Please provide a valid email address")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        String email,

        @Schema(description = "New first name", example = "John",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, minLength = 2, maxLength = 100)
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "First name can only contain letters," +
                " spaces, hyphens, and apostrophes")
        String firstName,

        @Schema(description = "New last name", example = "Doe",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, minLength = 2, maxLength = 100)
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name can only contain letters," +
                " spaces, hyphens, and apostrophes")
        String lastName,

        @Schema(description = "New password (min 8 chars with complexity requirements)",
                example = "NewSecurePass1!", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minLength = 8, maxLength = 100)
        @Size(min = 8, max = 100 , message = "The password length should be between 8 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long" +
                        " and contain at least one uppercase letter," +
                        " one lowercase letter, one digit, and one special character (@$!%*?&)"
        )
        String password,

        @Schema(description = "New user role (Admin only)", example = "MEMBER",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"ADMIN", "MEMBER"})
        Role role,

        @Schema(description = "Email verification status (Admin only)", example = "true",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean emailVerified,

        @Schema(description = "Profile picture URL", example = "https://example.com/avatar.jpg",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 500)
        @Size(max = 500, message = "Avatar URL cannot exceed 500 characters")
        String avatarUrl ,

        @Schema(description = "User status (Admin only)", example = "ACTIVE",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
        UserStatus status

) {


}