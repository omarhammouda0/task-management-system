package com.taskmanagement.team.dto;

import com.taskmanagement.team.enums.TeamRole;

import java.time.Instant;

public record TeamMemberResponseDto(

        Long id ,

        Long teamId,

        Long userId ,

        String userEmail ,

        String userFirstName ,

        String userLastName ,

        TeamRole role ,

        Instant joinedAt


) {
}
