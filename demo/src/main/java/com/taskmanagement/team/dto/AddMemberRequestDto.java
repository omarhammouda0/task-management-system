package com.taskmanagement.team.dto;

import com.taskmanagement.team.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for adding a member to a team")
public record AddMemberRequestDto(

        @Schema(description = "ID of the user to add to the team", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull (message = "User ID must not be null")
        Long userId ,

        @Schema(description = "ID of the team to add the user to", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull (message = "Team ID must not be null")
        Long teamId ,

        @Schema(description = "Role to assign to the member (defaults to MEMBER if not provided)",
                example = "MEMBER", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"OWNER", "ADMIN", "MEMBER"}, defaultValue = "MEMBER")
        TeamRole role

) {

    public AddMemberRequestDto {
        if (role == null) {
            role = TeamRole.MEMBER;
        }
    }
}
