package com.taskmanagement.auth.dto;


import com.taskmanagement.auth.role.ValidRole;
import com.taskmanagement.user.enums.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(

        @NotBlank (message = "User name cannot be blank")
        @Size(min = 2, max = 50 , message = "Username must be between 2 and 50 characters")
        String userName,

        @NotBlank (message = "Email name cannot be blank")
        @Email (message = "Email should be in a valid format")
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$",
                message = "Password must be at least 10 characters long " +
                        "and contain at least one uppercase letter," +
                        " one lowercase letter," +
                        " one digit," +
                        " and one special character" )
        String password,

        @NotBlank(message = "First name cannot be blank")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        String lastName,


        @NotNull(message = "Role is required")
        @ValidRole(allowedRoles = {Role.ADMIN, Role.MANAGER, Role.MEMBER},
                message = "Role must be either ADMIN, MANAGER, or MEMBER")
        Role role
) {
}


