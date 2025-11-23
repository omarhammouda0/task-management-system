package com.taskmanagement.team.dto;

import com.taskmanagement.team.enums.TeamRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleDto(

        @NotNull(message = "Member ID must not be null")
        Long memberId ,

        @NotNull(message = "Team ID must not be null")
        Long teamId ,

        @NotNull(message = "New role must not be null")
        TeamRole newRole


) {
}
