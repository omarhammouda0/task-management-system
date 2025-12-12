package com.taskmanagement.project.dto;

import com.taskmanagement.project.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request body for updating a project. All fields are optional - only provided fields will be updated.")
public record UpdateProjectDto(

        @Schema(description = "New project name", example = "Updated Project Name",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 100)
        @Size(max = 100, message = "Project name must not exceed 100 characters")
        String name ,

        @Schema(description = "New project description", example = "Updated project description",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 500)
        @Size(max = 500, message = "Project description must not exceed 500 characters")
        String description ,

        @Schema(description = "New project status (must follow valid transitions)",
                example = "ACTIVE", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"PLANNED", "ACTIVE", "ON_HOLD", "COMPLETED", "ARCHIVED"})
        ProjectStatus status ,

        @Schema(description = "New start date (ISO 8601 format)",
                example = "2025-01-15T00:00:00Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Instant startDate ,

        @Schema(description = "New end date (ISO 8601 format)",
                example = "2025-06-30T23:59:59Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Instant endDate
) {
}

