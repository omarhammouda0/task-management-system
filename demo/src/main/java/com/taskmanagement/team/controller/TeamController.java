package com.taskmanagement.team.controller;

import com.taskmanagement.team.dto.TeamCreateDto;
import com.taskmanagement.team.dto.TeamResponseDto;
import com.taskmanagement.team.dto.TeamUpdateDto;
import com.taskmanagement.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Team management endpoints - create, update, and manage teams for collaboration")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

    private final TeamService teamService;

    @Operation(
            summary = "Create a new team",
            description = "Creates a new team. The authenticated user becomes the team owner."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Team created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Team name already exists")
    })
    @PostMapping
    public ResponseEntity<TeamResponseDto> createTeam(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Team creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Create Team",
                                    value = """
                                            {
                                                "name": "Development Team",
                                                "description": "Backend and frontend developers"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody TeamCreateDto teamCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(teamCreateDto));
    }

    @Operation(
            summary = "Get all teams (Admin)",
            description = "Retrieves all teams in the system including deleted ones. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teams retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/all-teams")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TeamResponseDto>> getAllTeamsForAdmin(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getAllTeamsForAdmin(pageable));
    }

    @Operation(
            summary = "Get my teams",
            description = "Retrieves all teams that the authenticated user is a member of"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teams retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-teams")
    public ResponseEntity<Page<TeamResponseDto>> getMyTeams(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getMyTeams(pageable));
    }

    @Operation(
            summary = "Get team by ID",
            description = "Retrieves detailed information about a specific team"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponseDto> getTeamById(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamById(teamId));
    }

    @Operation(
            summary = "Get teams by owner",
            description = "Retrieves all teams owned by a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teams retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<TeamResponseDto>> getTeamsByOwner(
            @Parameter(description = "Owner user ID", required = true, example = "1")
            @PathVariable Long ownerId,
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamsByOwner(ownerId, pageable));
    }

    @Operation(
            summary = "Search team by name",
            description = "Searches for a team by its exact name"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @GetMapping("/search")
    public ResponseEntity<TeamResponseDto> searchTeamsByName(
            @Parameter(description = "Team name to search", required = true, example = "Development Team")
            @RequestParam("name") String name) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamByName(name));
    }

    @Operation(
            summary = "Update a team",
            description = "Updates an existing team's information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to update this team"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponseDto> updateTeam(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Team update details",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Update Team",
                                    value = """
                                            {
                                                "name": "Updated Team Name",
                                                "description": "Updated team description"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody TeamUpdateDto teamUpdateDto) {
        return ResponseEntity.ok(teamService.updateTeam(teamId, teamUpdateDto));
    }

    @Operation(
            summary = "Restore a deleted team (Admin)",
            description = "Restores a previously deleted team. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team restored successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @PutMapping("/restore/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamResponseDto> restoreTeam(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.restoreTeam(teamId));
    }

    @Operation(
            summary = "Delete a team",
            description = "Soft deletes a team. The team can be restored by an admin."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Team deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to delete this team"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}