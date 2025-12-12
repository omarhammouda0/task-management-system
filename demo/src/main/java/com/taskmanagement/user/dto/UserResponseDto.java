package com.taskmanagement.user.dto;

import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response containing user details")
public record UserResponseDto(

        @Schema(description = "Unique user ID", example = "1")
        Long id ,

        @Schema(description = "User email address", example = "john.doe@example.com")
        String email ,

        @Schema(description = "User's first name", example = "John")
        String firstName ,

        @Schema(description = "User's last name", example = "Doe")
        String lastName ,

        @Schema(description = "User role in the system", example = "MEMBER")
        Role role ,

        @Schema(description = "Whether user's email is verified", example = "true")
        Boolean emailVerified ,

        @Schema(description = "URL to user's profile picture", example = "https://example.com/avatar.jpg")
        String avatarUrl ,

        @Schema(description = "Timestamp when user was created", example = "2025-01-15T10:30:00Z")
        Instant createdAt ,

        @Schema(description = "Timestamp when user was last updated", example = "2025-01-15T10:30:00Z")
        Instant updatedAt ,

        @Schema(description = "Current user status", example = "ACTIVE")
        UserStatus status


) {
}


