package com.taskmanagement.team.dto;

import com.taskmanagement.team.enums.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response containing team details")
public record TeamResponseDto (

        @Schema(description = "Unique team ID", example = "1")
        Long id ,

        @Schema(description = "Team name", example = "Development Team")
        String name,

        @Schema(description = "Team description", example = "Backend development team for core services")
        String description,

        @Schema(description = "ID of the team owner", example = "1")
        Long ownerId,

        @Schema(description = "Current team status", example = "ACTIVE")
        TeamStatus status ,

        @Schema(description = "Timestamp when team was created", example = "2025-01-15T10:30:00Z")
        Instant createdAt
) {
}
