package com.taskmanagement.team.service;

import com.taskmanagement.team.dto.TeamCreateDto;
import com.taskmanagement.team.dto.TeamResponseDto;
import com.taskmanagement.team.dto.TeamUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface TeamService {

    TeamResponseDto createTeam(TeamCreateDto teamCreateDto);


    Page <TeamResponseDto> getAllTeamsForAdmin (Pageable pageable);

    Page<TeamResponseDto> getMyTeams(Pageable pageable);

    TeamResponseDto getTeamById (Long teamId);

    Page <TeamResponseDto> getTeamsByOwner(Long ownerId, Pageable pageable);

    TeamResponseDto getTeamByName(String teamName);

    TeamResponseDto updateTeam(Long teamId, TeamUpdateDto teamUpdateDto);

    void deleteTeam(Long teamId);


    TeamResponseDto restoreTeam(Long teamId);


}
