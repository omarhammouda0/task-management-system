package com.taskmanagement.team.mapper;

import com.taskmanagement.team.dto.AddMemberRequestDto;
import com.taskmanagement.team.dto.TeamMemberResponseDto;
import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.entity.TeamMember;
import com.taskmanagement.team.enums.TeamMemberStatus;
import com.taskmanagement.team.enums.TeamRole;
import com.taskmanagement.user.entity.User;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class TeamMemberMapper {


    public TeamMember toEntity(AddMemberRequestDto dto, Team team, User user) {
        return TeamMember.builder()
                .team(team)
                .user(user)
                .role(dto.role() == null ? TeamRole.MEMBER : dto.role())
                .status(TeamMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();
    }


    public TeamMember toOwnerEntity(Team team, User owner) {
        return TeamMember.builder()
                .team(team)
                .user(owner)
                .role(TeamRole.OWNER)
                .status(TeamMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();
    }

    public TeamMemberResponseDto toDto(TeamMember member, User user) {
        return new TeamMemberResponseDto(
                member.getId(),
                member.getTeamId(),
                member.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                member.getRole(),
                member.getJoinedAt()
        );
    }


    public TeamMemberResponseDto toDto(TeamMember member) {
        User user = member.getUser();
        return new TeamMemberResponseDto(
                member.getId(),
                member.getTeamId(),
                member.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                member.getRole(),
                member.getJoinedAt()
        );

    }
}