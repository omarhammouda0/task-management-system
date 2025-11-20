package com.taskmanagement.user.dto;

import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import jakarta.validation.constraints.*;


public record UserCreateDto (

        @Email (message = "Please enter a valid email address")
        @NotNull (message = "Email is required")
        @NotBlank (message = "Email is required")
        @Size ( max = 255, message = "Email address can not be more than 255 characters")
        String email ,


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


        @NotBlank (message = "First name can not be blank")
        @NotNull (message = "First name is required")
        @Size(min = 2, max = 100 , message = "The first name length must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "First name can only contain letters," +
                " spaces, hyphens, and apostrophes")

        String firstName ,


        @NotBlank (message = "Last name can not be blank")
        @NotNull (message = "Last name is required")
        @Size(min = 2, max = 100 , message = "The last name length must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name can only contain letters," +
                " spaces, hyphens, and apostrophes")

        String lastName ,

        Role role ,

        UserStatus userStatus


) {


}

