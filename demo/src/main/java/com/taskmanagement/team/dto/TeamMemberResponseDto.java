package com.taskmanagement.team.dto;

import com.taskmanagement.team.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response containing team member details")
public record TeamMemberResponseDto(

        @Schema(description = "Unique team member record ID", example = "1")
        Long id ,

        @Schema(description = "ID of the team", example = "1")
        Long teamId,

        @Schema(description = "ID of the user", example = "1")
        Long userId ,

        @Schema(description = "Email of the team member", example = "john.doe@example.com")
        String userEmail ,

        @Schema(description = "First name of the team member", example = "John")
        String userFirstName ,

        @Schema(description = "Last name of the team member", example = "Doe")
        String userLastName ,

        @Schema(description = "Role of the member in this team", example = "MEMBER")
        TeamRole role ,

        @Schema(description = "Timestamp when user joined the team", example = "2025-01-15T10:30:00Z")
        Instant joinedAt


) {
}
