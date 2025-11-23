package com.taskmanagement.team.mapper;

import com.taskmanagement.team.dto.TeamCreateDto;
import com.taskmanagement.team.dto.TeamResponseDto;
import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.enums.TeamStatus;
import org.springframework.stereotype.Service;

@Service
public class TeamMapper {

    public Team toEntity(TeamCreateDto dto) {

        return Team.builder ( )

                .name ( dto.name ( ).trim ( ) )
                .description ( dto.description ( ) == null ? null : dto.description ( ).trim ( ) )
                // OwnerId should be set in the service layer based on the authenticated user
                // Hence, I am not setting it here in the mapper
                .status ( TeamStatus.ACTIVE )

                .build ( );

    }

    public TeamResponseDto toDto(Team entity) {

        return new TeamResponseDto (

                entity.getId (),
                entity.getName () ,
                entity.getDescription ( ) ,
                entity.getOwnerId ( ) ,
                entity.getStatus ( ) ,
                entity.getCreatedAt ( )
        );

    }

}
