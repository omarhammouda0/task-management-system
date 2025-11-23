package com.taskmanagement.team.mapper;

import com.taskmanagement.team.dto.AddMemberRequestDto;
import com.taskmanagement.team.dto.TeamMemberResponseDto;
import com.taskmanagement.team.entity.TeamMember;
import com.taskmanagement.team.enums.TeamRole;
import com.taskmanagement.user.entity.User;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class TeamMemberMapper {

    public TeamMember toEntity(AddMemberRequestDto dto) {

        return TeamMember.builder ( )

                .teamId ( dto.teamId ( ) )
                .userId ( dto.userId ( ) )
                .role ( dto.role ( ) == null ? TeamRole.MEMBER : dto.role ( ) )
                .joinedAt ( Instant.now () )

                .build ( );

    }

    public TeamMemberResponseDto toDto(TeamMember member , User user) {

        return new TeamMemberResponseDto (

                member.getId ( ) ,
                member.getTeamId ( ) ,
                member.getUserId ( ) ,
                user.getEmail ( ) ,
                user.getFirstName ( ) ,
                user.getLastName ( ) ,
                member.getRole ( ) ,
                member.getJoinedAt ( )


        );

    }
}