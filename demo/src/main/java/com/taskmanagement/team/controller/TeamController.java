package com.taskmanagement.team.controller;

import com.taskmanagement.team.dto.TeamCreateDto;
import com.taskmanagement.team.dto.TeamResponseDto;
import com.taskmanagement.team.dto.TeamUpdateDto;
import com.taskmanagement.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor

@RestController
@RequestMapping("/api/teams")

public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponseDto> createTeam(@Valid @RequestBody TeamCreateDto teamCreateDto) {
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( teamService.createTeam ( teamCreateDto ) );

    }

    @GetMapping("/all-teams")
    @PreAuthorize ( "hasRole('ADMIN')" )
    public ResponseEntity<Page<TeamResponseDto>> getAllTeamsForAdmin (Pageable pageable) {
        return ResponseEntity.status ( HttpStatus.OK ).body ( teamService.getAllTeamsForAdmin ( pageable ) );
    }

    @GetMapping("/my-teams")
    public ResponseEntity<Page<TeamResponseDto>> getMyTeams
            (Pageable pageable) {
        return ResponseEntity.status ( HttpStatus.OK ).body ( teamService.getMyTeams ( pageable ) );
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponseDto> getTeamById
            (@PathVariable Long teamId) {

        return ResponseEntity.status ( HttpStatus.OK ).body ( teamService.getTeamById ( teamId ) );
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity <Page<TeamResponseDto>> getTeamsByOwner
            (@PathVariable Long ownerId , Pageable pageable) {
        return ResponseEntity.status ( HttpStatus.OK ).body ( teamService.getTeamsByOwner (ownerId , pageable ) );
    }

    @GetMapping("/search")
    public ResponseEntity <TeamResponseDto> searchTeamsByName
            (@RequestParam("name") String name  ) {
        return ResponseEntity.status ( HttpStatus.OK ).body ( teamService.getTeamByName ( name  ) );
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponseDto> updateTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamUpdateDto teamUpdateDto) {
        return ResponseEntity.ok(teamService.updateTeam(teamId, teamUpdateDto));
    }

    @PutMapping("/restore/{teamId}")
    @PreAuthorize ( "hasRole('ADMIN')" )
    public ResponseEntity<TeamResponseDto> restoreTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.restoreTeam(teamId));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long teamId) {

        teamService.deleteTeam(teamId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}