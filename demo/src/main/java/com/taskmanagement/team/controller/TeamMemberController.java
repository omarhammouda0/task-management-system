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
@Tag(name = "Team Members", description = "Team membership management - add, remove, and manage team members")
@SecurityRequirement(name = "bearerAuth")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @Operation(
            summary = "Add member to team",
            description = "Adds a user as a member to a team with a specified role"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Team or user not found"),
            @ApiResponse(responseCode = "409", description = "User is already a member of the team")
    })
    @PostMapping
    public ResponseEntity<TeamMemberResponseDto> addMember(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Member addition details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Add Member",
                                    value = """
                                            {
                                                "teamId": 1,
                                                "userId": 5,
                                                "role": "MEMBER"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody AddMemberRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamMemberService.addMember(dto));
    }

    @Operation(
            summary = "Get team members",
            description = "Retrieves all members of a specific team with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @GetMapping("/{teamId}/members")
    public ResponseEntity<Page<TeamMemberResponseDto>> getMembersByTeam(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teamMemberService.getMembersByTeam(teamId, pageable));
    }

    @Operation(
            summary = "Get specific team member",
            description = "Retrieves information about a specific member in a team"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Team or member not found")
    })
    @GetMapping("/{teamId}/members/{userId}")
    public ResponseEntity<TeamMemberResponseDto> getMember(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "User ID", required = true, example = "5")
            @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(teamMemberService.getMember(teamId, userId));
    }

    @Operation(
            summary = "Get total members count (Admin)",
            description = "Returns the total number of members in a team including inactive ones. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", example = "15"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
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
            summary = "Get active members count (Admin)",
            description = "Returns the number of active members in a team. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", example = "12"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
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
            description = "Updates the role of a team member"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member role updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to update member role"),
            @ApiResponse(responseCode = "404", description = "Team or member not found")
    })
    @PutMapping
    public ResponseEntity<TeamMemberResponseDto> updateMemberRole(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role update details",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Update Role",
                                    value = """
                                            {
                                                "teamId": 1,
                                                "userId": 5,
                                                "role": "MANAGER"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UpdateMemberRoleDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(teamMemberService.updateMemberRole(dto));
    }

    @Operation(
            summary = "Remove member from team",
            description = "Removes a user from a team"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to remove this member"),
            @ApiResponse(responseCode = "404", description = "Team or member not found")
    })
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "User ID to remove", required = true, example = "5")
            @PathVariable Long userId) {
        teamMemberService.removeMember(teamId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Leave team",
            description = "Allows the authenticated user to leave a team they are a member of"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully left the team"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Team not found or user is not a member")
    })
    @DeleteMapping("/{teamId}/leave")
    public ResponseEntity<Void> leaveTeam(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId) {
        teamMemberService.leaveTeam(teamId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}