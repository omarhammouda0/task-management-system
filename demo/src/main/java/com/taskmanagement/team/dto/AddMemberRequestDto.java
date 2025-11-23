package com.taskmanagement.team.dto;

import com.taskmanagement.team.enums.TeamRole;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequestDto(

        @NotNull (message = "User ID must not be null")
        Long userId ,

        @NotNull (message = "Team ID must not be null")
        Long teamId ,

        TeamRole role

) {

    public AddMemberRequestDto {
        if (role == null) {
            role = TeamRole.MEMBER;
        }
    }
}
