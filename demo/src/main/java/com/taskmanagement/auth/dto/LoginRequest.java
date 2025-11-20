package com.taskmanagement.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Email name cannot be blank")
        @Email(message = "Email should be in a valid format")
        String email,

        @NotBlank(message = "Password cannot be blank")
        String password

) {
}
