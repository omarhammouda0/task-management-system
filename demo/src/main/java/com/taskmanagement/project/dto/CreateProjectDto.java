package com.taskmanagement.project.dto;

import com.taskmanagement.project.enums.ProjectStatus;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateProjectDto(

        @NotNull (message = "Team ID is required")
        Long teamId ,

        @NotBlank (message = "Project name is required")
        @Size (max = 100 , message = "Project name must not exceed 100 characters")
        String name ,

        @Size (max = 500 , message = "Project description must not exceed 500 characters")
        String description ,

        ProjectStatus status ,

        Instant startDate ,

        Instant endDate


) {


}

