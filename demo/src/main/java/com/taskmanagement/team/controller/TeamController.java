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
            description = """
                    Creates a new team in the system.
                    
                    **Business Logic:**
                    - The authenticated user automatically becomes the team OWNER
                    - A TeamMember record is created with OWNER role for the creator
                    - Team name must be unique across the system (case-insensitive)
                    - Team status is set to ACTIVE by default
                    
                    **Authorization:**
                    - Any authenticated and active user can create a team
                    
                    **Validation:**
                    - Team name is required and must be 2-100 characters
                    - Team name must be unique (case-insensitive)
                    - Description is optional, max 500 characters
                    - User must have ACTIVE status
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Team created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - name too short/long or blank"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User account is not active"),
            @ApiResponse(responseCode = "409", description = "Conflict - Team name already exists")
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
            summary = "Get all teams (Admin only)",
            description = """
                    Retrieves all teams in the system with pagination support.
                    
                    **Business Logic:**
                    - Returns ALL teams including INACTIVE and DELETED ones
                    - Only system administrators can access this endpoint
                    - Useful for system-wide team management and auditing
                    
                    **Authorization:**
                    - Requires ADMIN role
                    - User must have ACTIVE status
                    
                    **Pagination:**
                    - Supports page, size, and sort parameters
                    - Default page size is 20
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teams retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required or user not active")
    })
    @GetMapping("/all-teams")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TeamResponseDto>> getAllTeamsForAdmin(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getAllTeamsForAdmin(pageable));
    }

    @Operation(
            summary = "Get my teams",
            description = """
                    Retrieves all ACTIVE teams that the authenticated user is a member of.
                    
                    **Business Logic:**
                    - Returns only teams where user has an ACTIVE membership
                    - Includes teams where user is OWNER, ADMIN, or MEMBER
                    - Only returns teams with ACTIVE status
                    
                    **Authorization:**
                    - Any authenticated and active user can access their own teams
                    
                    **Pagination:**
                    - Supports page, size, and sort parameters
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teams retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User account is not active")
    })
    @GetMapping("/my-teams")
    public ResponseEntity<Page<TeamResponseDto>> getMyTeams(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getMyTeams(pageable));
    }

    @Operation(
            summary = "Get team by ID",
            description = """
                    Retrieves detailed information about a specific team.
                    
                    **Business Logic:**
                    - Returns team details if found and ACTIVE
                    - User must be a member of the team to view it
                    - System admins can view any team
                    
                    **Authorization:**
                    - Team members (OWNER, ADMIN, MEMBER) can view team details
                    - System ADMIN can view any team
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team ID must not be null
                    - Team must exist and be ACTIVE
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a member of this team or user not active"),
            @ApiResponse(responseCode = "404", description = "Team not found or not active")
    })
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponseDto> getTeamById(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamById(teamId));
    }

    @Operation(
            summary = "Get teams by owner",
            description = """
                    Retrieves all teams owned by a specific user.
                    
                    **Business Logic:**
                    - For regular users: Can only view their own teams (ACTIVE status only)
                    - For admins: Can view any user's teams (all statuses)
                    
                    **Authorization:**
                    - Users can only query their own owned teams
                    - System ADMIN can query any user's owned teams
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Owner ID must not be null
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teams retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot view other users' teams (unless admin)")
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<TeamResponseDto>> getTeamsByOwner(
            @Parameter(description = "Owner user ID", required = true, example = "1")
            @PathVariable Long ownerId,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamsByOwner(ownerId, pageable));
    }

    @Operation(
            summary = "Search team by name",
            description = """
                    Searches for a team by its exact name (case-insensitive).
                    
                    **Business Logic:**
                    - Performs case-insensitive exact match on team name
                    - Only returns ACTIVE teams
                    - User must be a member of the team to view it (unless admin)
                    
                    **Authorization:**
                    - Team members can search and view their teams
                    - System ADMIN can search any team
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team name must not be null or empty
                    - Name is trimmed before search
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - Team name is empty"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a member of this team"),
            @ApiResponse(responseCode = "404", description = "Team not found or not active")
    })
    @GetMapping("/search")
    public ResponseEntity<TeamResponseDto> searchTeamsByName(
            @Parameter(description = "Team name to search (exact match, case-insensitive)", required = true, example = "Development Team")
            @RequestParam("name") String name) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamByName(name));
    }

    @Operation(
            summary = "Update a team",
            description = """
                    Updates an existing team's information.
                    
                    **Business Logic:**
                    - Only team OWNER or system ADMIN can update team details
                    - Team name uniqueness is validated (excluding current team)
                    - At least one field must be provided for update
                    - Team must be ACTIVE to be updated
                    
                    **Authorization:**
                    - Team OWNER can update their team
                    - System ADMIN can update any team
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist and be ACTIVE
                    - New name (if provided) must be unique and 2-100 characters
                    - Description (if provided) max 500 characters
                    - At least one field must be provided
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - validation failed or no fields provided"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not team owner or admin"),
            @ApiResponse(responseCode = "404", description = "Team not found or not active"),
            @ApiResponse(responseCode = "409", description = "Conflict - New team name already exists")
    })
    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponseDto> updateTeam(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Team update details - at least one field required",
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
            summary = "Restore a deleted team (Admin only)",
            description = """
                    Restores a previously deleted team back to ACTIVE status.
                    
                    **Business Logic:**
                    - Only DELETED teams can be restored
                    - Team is restored to ACTIVE status
                    - All team memberships remain intact
                    - Team name uniqueness is re-validated
                    
                    **Authorization:**
                    - Requires ADMIN role
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist
                    - Team must be in DELETED status
                    - Team name must still be unique
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team restored successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - Team name now conflicts with existing team"),
            @ApiResponse(responseCode = "422", description = "Unprocessable - Team is not in DELETED status")
    })
    @PutMapping("/restore/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamResponseDto> restoreTeam(
            @Parameter(description = "Team ID to restore", required = true, example = "1")
            @PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.restoreTeam(teamId));
    }

    @Operation(
            summary = "Delete a team",
            description = """
                    Soft deletes a team (changes status to DELETED).
                    
                    **Business Logic:**
                    - Performs soft delete - team can be restored by admin
                    - Only team OWNER or system ADMIN can delete a team
                    - Team status changes from ACTIVE to DELETED
                    - Team members are NOT automatically removed
                    - Projects under this team become inaccessible
                    
                    **Authorization:**
                    - Team OWNER can delete their own team
                    - System ADMIN can delete any team
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist
                    - Team must be in ACTIVE status
                    - Cannot delete already deleted teams
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Team deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not team owner or admin"),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "422", description = "Unprocessable - Team is not in ACTIVE status")
    })
    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "Team ID to delete", required = true, example = "1")
            @PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}