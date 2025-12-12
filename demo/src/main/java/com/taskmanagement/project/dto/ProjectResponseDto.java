package com.taskmanagement.project.dto;

import com.taskmanagement.project.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response containing project details")
public record ProjectResponseDto(

        @Schema(description = "Unique project ID", example = "1")
        Long id ,

        @Schema(description = "Project name", example = "Website Redesign")
        String name ,

        @Schema(description = "Project description", example = "Redesign the company website with modern UI/UX")
        String description ,

        @Schema(description = "ID of the team this project belongs to", example = "1")
        Long teamId ,

        @Schema(description = "Current project status", example = "ACTIVE")
        ProjectStatus status ,

        @Schema(description = "Project start date", example = "2025-01-15T00:00:00Z")
        Instant startDate ,

        @Schema(description = "Project end date", example = "2025-06-30T23:59:59Z")
        Instant endDate ,

        @Schema(description = "ID of user who created this project", example = "1")
        Long createdBy ,

        @Schema(description = "Timestamp when project was created", example = "2025-01-15T10:30:00Z")
        Instant createdAt ,

        @Schema(description = "Timestamp when project was last updated", example = "2025-01-15T10:30:00Z")
        Instant updatedAt


) {
}

