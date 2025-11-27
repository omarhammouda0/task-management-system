package com.taskmanagement.project.dto;

import com.taskmanagement.project.enums.ProjectStatus;

import java.time.Instant;

public record ProjectResponseDto(

        Long id ,
        String name ,
        String description ,
        Long teamId ,
        ProjectStatus status ,
        Instant startDate ,
        Instant endDate ,
        Long createdBy ,
        Instant createdAt ,
        Instant updatedAt


) {
}

