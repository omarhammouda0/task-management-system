package com.taskmanagement.user.dto;

import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;

import java.time.Instant;

public record UserResponseDto(

        Long id ,
        String email ,
        String firstName ,
        String lastName ,
        Role role ,
        Boolean emailVerified ,
        String avatarUrl ,
        Instant createdAt ,
        Instant updatedAt ,
        UserStatus status


) {
}


