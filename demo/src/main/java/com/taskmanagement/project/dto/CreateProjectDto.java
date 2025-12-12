package com.taskmanagement.project.dto;

import com.taskmanagement.project.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request body for creating a new project")
public record CreateProjectDto(

        @Schema(description = "ID of the team this project belongs to", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull (message = "Team ID is required")
        Long teamId ,

        @Schema(description = "Project name (must be unique within the team)", example = "Website Redesign",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
        @NotBlank (message = "Project name is required")
        @Size (max = 100 , message = "Project name must not exceed 100 characters")
        String name ,

        @Schema(description = "Project description", example = "Redesign the company website with modern UI/UX",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 500)
        @Size (max = 500 , message = "Project description must not exceed 500 characters")
        String description ,

        @Schema(description = "Initial project status (defaults to PLANNED if not provided)",
                example = "PLANNED", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"PLANNED", "ACTIVE", "ON_HOLD", "COMPLETED", "ARCHIVED"})
        ProjectStatus status ,

        @Schema(description = "Project start date (ISO 8601 format)",
                example = "2025-01-15T00:00:00Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Instant startDate ,

        @Schema(description = "Project end date (ISO 8601 format)",
                example = "2025-06-30T23:59:59Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Instant endDate


) {


}

