package com.taskmanagement.team.controller;

import com.taskmanagement.team.dto.*;
import com.taskmanagement.team.service.TeamMemberService;
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
@RequestMapping("/api/team-members")
@Tag(name = "Team Members", description = "Team membership management - add, remove, and manage team members with role-based access control")
@SecurityRequirement(name = "bearerAuth")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @Operation(
            summary = "Add member to team",
            description = """
                    Adds a user as a member to a team with a specified role.
                    
                    **Business Logic:**
                    - Only team OWNER can add new members to the team
                    - User must not already be a member of the team
                    - New member status is set to ACTIVE
                    - Available roles: MEMBER, ADMIN (OWNER role cannot be assigned)
                    
                    **Authorization:**
                    - Only team OWNER can add members
                    - User must have ACTIVE status
                    - Target user must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist and be ACTIVE
                    - User must exist and be ACTIVE
                    - User must not already be a member
                    - Role must be valid (MEMBER or ADMIN)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - invalid role or missing fields"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only team owner can add members"),
            @ApiResponse(responseCode = "404", description = "Team or user not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - User is already a member of the team")
    })
    @PostMapping
    public ResponseEntity<TeamMemberResponseDto> addMember(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Member addition details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Add as Member",
                                            value = """
                                                    {
                                                        "teamId": 1,
                                                        "userId": 5,
                                                        "role": "MEMBER"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Add as Admin",
                                            value = """
                                                    {
                                                        "teamId": 1,
                                                        "userId": 5,
                                                        "role": "ADMIN"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody AddMemberRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamMemberService.addMember(dto));
    }

    @Operation(
            summary = "Get team members",
            description = """
                    Retrieves all ACTIVE members of a specific team with pagination.
                    
                    **Business Logic:**
                    - Returns only members with ACTIVE status
                    - Includes member details: email, name, role, joined date
                    - Results are paginated
                    
                    **Authorization:**
                    - Team OWNER or ADMIN can view all members
                    - System ADMIN can view members of any team
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist and be ACTIVE
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not team owner or admin"),
            @ApiResponse(responseCode = "404", description = "Team not found or not active")
    })
    @GetMapping("/{teamId}/members")
    public ResponseEntity<Page<TeamMemberResponseDto>> getMembersByTeam(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teamMemberService.getMembersByTeam(teamId, pageable));
    }

    @Operation(
            summary = "Get specific team member",
            description = """
                    Retrieves detailed information about a specific member in a team.
                    
                    **Business Logic:**
                    - Returns member details including role and join date
                    - Users can view their own membership details
                    
                    **Authorization:**
                    - Team OWNER or ADMIN can view any member's details
                    - Users can view their own membership (self-operation)
                    - System ADMIN can view any member
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist and be ACTIVE
                    - User must be a member of the team
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot view this member's details"),
            @ApiResponse(responseCode = "404", description = "Team not found or user is not a member")
    })
    @GetMapping("/{teamId}/members/{userId}")
    public ResponseEntity<TeamMemberResponseDto> getMember(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "User ID of the member", required = true, example = "5")
            @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(teamMemberService.getMember(teamId, userId));
    }

    @Operation(
            summary = "Get total members count (Admin only)",
            description = """
                    Returns the total number of members in a team including all statuses.
                    
                    **Business Logic:**
                    - Counts ALL members regardless of status (ACTIVE, INACTIVE, etc.)
                    - Useful for administrative reporting
                    
                    **Authorization:**
                    - Requires ADMIN role
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist (any status)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", example = "15"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @GetMapping("/{teamId}/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getTotalMembersCountInTeamForAdmin(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId) {
        return ResponseEntity.status(HttpStatus.OK).body(teamMemberService.getTotalMembersCountForAdmin(teamId));
    }

    @Operation(
            summary = "Get active members count (Admin only)",
            description = """
                    Returns the number of ACTIVE members in a team.
                    
                    **Business Logic:**
                    - Counts only members with ACTIVE status
                    - Useful for understanding actual team capacity
                    
                    **Authorization:**
                    - Requires ADMIN role
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist (any status)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", example = "12"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @GetMapping("/{teamId}/count/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getActiveMembersCount(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId) {
        return ResponseEntity.status(HttpStatus.OK).body(teamMemberService.getActiveMembersCount(teamId));
    }

    @Operation(
            summary = "Update member role",
            description = """
                    Updates the role of a team member.
                    
                    **Business Logic:**
                    - Only team OWNER can update member roles
                    - OWNER cannot update their own role (prevents accidental demotion)
                    - Role transitions are validated (e.g., cannot demote last owner)
                    - New role must be different from current role
                    
                    **Role Hierarchy:**
                    - OWNER: Full control, can manage all members and team settings
                    - ADMIN: Can manage members but cannot modify team settings
                    - MEMBER: Basic access, can view and participate
                    
                    **Authorization:**
                    - Only team OWNER can update roles
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist and be ACTIVE
                    - Member must exist in the team
                    - New role must be different from current
                    - Cannot demote the last OWNER
                    - OWNER cannot modify their own role
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member role updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - same role or invalid role transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only team owner can update roles / Cannot modify own role"),
            @ApiResponse(responseCode = "404", description = "Team not found or user is not a member"),
            @ApiResponse(responseCode = "422", description = "Unprocessable - Cannot demote last owner")
    })
    @PutMapping
    public ResponseEntity<TeamMemberResponseDto> updateMemberRole(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role update details",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Promote to Admin",
                                            value = """
                                                    {
                                                        "memberId": 5,
                                                        "teamId": 1,
                                                        "newRole": "ADMIN"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Demote to Member",
                                            value = """
                                                    {
                                                        "memberId": 5,
                                                        "teamId": 1,
                                                        "newRole": "MEMBER"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody UpdateMemberRoleDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(teamMemberService.updateMemberRole(dto));
    }

    @Operation(
            summary = "Remove member from team",
            description = """
                    Removes a user from a team.
                    
                    **Business Logic:**
                    - Only team OWNER can remove members
                    - OWNER cannot remove themselves (must transfer ownership or delete team)
                    - Member status is changed (soft delete)
                    - Member's tasks may be unassigned
                    
                    **Authorization:**
                    - Only team OWNER can remove members
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist and be ACTIVE
                    - Member must exist in the team
                    - Cannot remove the team owner
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only team owner can remove members / Cannot remove owner"),
            @ApiResponse(responseCode = "404", description = "Team not found or user is not a member")
    })
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "User ID to remove from team", required = true, example = "5")
            @PathVariable Long userId) {
        teamMemberService.removeMember(teamId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Leave team",
            description = """
                    Allows the authenticated user to leave a team they are a member of.
                    
                    **Business Logic:**
                    - User voluntarily leaves the team
                    - The LAST OWNER cannot leave (must transfer ownership first)
                    - The LAST ACTIVE MEMBER cannot leave (would orphan the team)
                    - Member status is changed to INACTIVE
                    
                    **Authorization:**
                    - Any team member can leave their team
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist and be ACTIVE
                    - User must be a member of the team
                    - Cannot be the last owner
                    - Cannot be the last active member
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully left the team"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Last owner or last active member cannot leave"),
            @ApiResponse(responseCode = "404", description = "Team not found or user is not a member")
    })
    @DeleteMapping("/{teamId}/leave")
    public ResponseEntity<Void> leaveTeam(
            @Parameter(description = "Team ID to leave", required = true, example = "1")
            @PathVariable Long teamId) {
        teamMemberService.leaveTeam(teamId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}