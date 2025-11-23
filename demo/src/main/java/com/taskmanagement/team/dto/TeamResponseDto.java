package com.taskmanagement.team.dto;

import com.taskmanagement.team.enums.TeamStatus;

import java.time.Instant;

public record TeamResponseDto (

        Long id ,

        String name,

        String description,

        Long ownerId,

        TeamStatus status ,

        Instant createdAt
) {
}
