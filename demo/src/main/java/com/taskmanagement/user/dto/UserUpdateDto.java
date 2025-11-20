package com.taskmanagement.user.dto;

import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UserUpdateDto(

        @Email(message = "Please provide a valid email address")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        String email,

        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "First name can only contain letters," +
                " spaces, hyphens, and apostrophes")

        String firstName,

        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name can only contain letters," +
                " spaces, hyphens, and apostrophes")

        String lastName,

        @Size(min = 8, max = 100 , message = "The password length should be between 8 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long" +
                        " and contain at least one uppercase letter," +
                        " one lowercase letter, one digit, and one special character (@$!%*?&)"
        )


        String password,

        Role role,

        Boolean emailVerified,

        @Size(max = 500, message = "Avatar URL cannot exceed 500 characters")
        String avatarUrl ,

        UserStatus status

) {


}