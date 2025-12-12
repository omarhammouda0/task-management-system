package com.taskmanagement.user.dto;

import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request body for creating a new user (Admin only)")
public record UserCreateDto (

        @Schema(description = "User email address (must be unique)", example = "john.doe@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
        @Email (message = "Please enter a valid email address")
        @NotNull (message = "Email is required")
        @NotBlank (message = "Email is required")
        @Size ( max = 255, message = "Email address can not be more than 255 characters")
        String email ,

        @Schema(description = "Password (min 8 chars, must include uppercase, lowercase, digit, and special character)",
                example = "SecurePass1!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 100)
        @NotBlank (message = "Password can not be blank")
        @NotNull (message = "Password is required")
        @Size(min = 8, max = 100 , message = "The password length should be between 8 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long" +
                        " and contain at least one uppercase letter," +
                        " one lowercase letter, one digit, and one special character (@$!%*?&)"
        )
        String password ,

        @Schema(description = "User's first name", example = "John",
                requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)
        @NotBlank (message = "First name can not be blank")
        @NotNull (message = "First name is required")
        @Size(min = 2, max = 100 , message = "The first name length must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "First name can only contain letters," +
                " spaces, hyphens, and apostrophes")
        String firstName ,

        @Schema(description = "User's last name", example = "Doe",
                requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)
        @NotBlank (message = "Last name can not be blank")
        @NotNull (message = "Last name is required")
        @Size(min = 2, max = 100 , message = "The last name length must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name can only contain letters," +
                " spaces, hyphens, and apostrophes")
        String lastName ,

        @Schema(description = "User role (defaults to MEMBER, ignored for non-admin users)",
                example = "MEMBER", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"ADMIN", "MEMBER"}, defaultValue = "MEMBER")
        Role role ,

        @Schema(description = "Initial user status (defaults to ACTIVE)",
                example = "ACTIVE", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
        UserStatus userStatus


) {


}

