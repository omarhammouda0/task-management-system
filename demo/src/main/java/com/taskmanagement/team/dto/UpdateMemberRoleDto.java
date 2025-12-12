package com.taskmanagement.team.dto;

import com.taskmanagement.team.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for updating a team member's role")
public record UpdateMemberRoleDto(

        @Schema(description = "ID of the team member record to update", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Member ID must not be null")
        Long memberId ,

        @Schema(description = "ID of the team", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Team ID must not be null")
        Long teamId ,

        @Schema(description = "New role to assign to the member", example = "ADMIN",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"OWNER", "ADMIN", "MEMBER"})
        @NotNull(message = "New role must not be null")
        TeamRole newRole


) {
}
