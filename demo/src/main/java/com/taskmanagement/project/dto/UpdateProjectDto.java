package com.taskmanagement.project.dto;

import com.taskmanagement.project.enums.ProjectStatus;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateProjectDto(

        @Size(max = 100, message = "Project name must not exceed 100 characters")
        String name ,

        @Size(max = 500, message = "Project description must not exceed 500 characters")
        String description ,

        ProjectStatus status ,

        Instant startDate ,

        Instant endDate
) {
}

