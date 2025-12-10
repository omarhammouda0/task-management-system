package com.taskmanagement.project.controller;

import com.taskmanagement.project.dto.CreateProjectDto;
import com.taskmanagement.project.dto.ProjectResponseDto;
import com.taskmanagement.project.dto.TransferProjectDto;
import com.taskmanagement.project.dto.UpdateProjectDto;
import com.taskmanagement.project.service.ProjectService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management endpoints - create, update, archive, and manage projects within teams")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "Create a new project",
            description = """
                    Creates a new project associated with a team.
                    
                    **Business Logic:**
                    - Project is created under the specified team
                    - Project name must be unique within the team (case-insensitive)
                    - Initial status can be PLANNED, ACTIVE, or ON_HOLD (defaults to PLANNED)
                    - Start date must be in the future if provided
                    - End date must be after start date if both provided
                    - The authenticated user is recorded as the creator
                    
                    **Authorization:**
                    - Only team OWNER can create projects in their team
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist and be ACTIVE
                    - Project name: required, 2-100 characters, unique in team
                    - Description: optional, max 500 characters
                    - Status: optional (PLANNED, ACTIVE, ON_HOLD)
                    - Start date: optional, must be future date
                    - End date: optional, must be after start date
                    
                    **Project Status Flow:**
                    PLANNED → ACTIVE → COMPLETED → ARCHIVED
                              ↓         ↑
                           ON_HOLD ────┘
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - validation failed or invalid dates"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not team owner or user not active"),
            @ApiResponse(responseCode = "404", description = "Team not found or not active"),
            @ApiResponse(responseCode = "409", description = "Conflict - Project name already exists in team")
    })
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Project creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Basic Project",
                                            value = """
                                                    {
                                                        "name": "E-Commerce Platform",
                                                        "description": "Online shopping platform development",
                                                        "teamId": 1
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Project with Dates",
                                            value = """
                                                    {
                                                        "name": "Mobile App Development",
                                                        "description": "iOS and Android app",
                                                        "teamId": 1,
                                                        "status": "PLANNED",
                                                        "startDate": "2025-01-15T00:00:00Z",
                                                        "endDate": "2025-06-30T23:59:59Z"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody CreateProjectDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(dto));
    }

    @Operation(
            summary = "Restore a deleted project (Admin only)",
            description = """
                    Restores a DELETED project back to PLANNED status.
                    
                    **Business Logic:**
                    - Only DELETED projects can be restored
                    - Project is restored to PLANNED status
                    - Project name uniqueness is re-validated in the team
                    - Team must still be ACTIVE
                    
                    **Authorization:**
                    - Requires ADMIN role
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Project must exist
                    - Project must be in DELETED status
                    - Team must be ACTIVE
                    - Project name must still be unique in team
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project restored successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - Project name now conflicts with existing project"),
            @ApiResponse(responseCode = "422", description = "Unprocessable - Project is not in DELETED status or team inactive")
    })
    @PostMapping("/{projectId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDto> restoreProject(
            @Parameter(description = "Project ID to restore", required = true, example = "1")
            @PathVariable Long projectId) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.restoreProject(projectId));
    }

    @Operation(
            summary = "Activate a project (Admin only)",
            description = """
                    Changes project status to ACTIVE.
                    
                    **Business Logic:**
                    - Only PLANNED or ON_HOLD projects can be activated
                    - Project dates are re-validated upon activation
                    - Team must be ACTIVE
                    
                    **Valid Status Transitions to ACTIVE:**
                    - PLANNED → ACTIVE
                    - ON_HOLD → ACTIVE
                    
                    **Authorization:**
                    - Requires ADMIN role
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Project must exist
                    - Current status must be PLANNED or ON_HOLD
                    - Team must be ACTIVE
                    - Project dates must be valid
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project activated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "422", description = "Unprocessable - Invalid status transition or invalid dates")
    })
    @PostMapping("/{projectId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDto> activateProject(
            @Parameter(description = "Project ID to activate", required = true, example = "1")
            @PathVariable Long projectId) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.activateProject(projectId));
    }

    @Operation(
            summary = "Archive a project (Admin only)",
            description = """
                    Archives a completed or on-hold project.
                    
                    **Business Logic:**
                    - Only COMPLETED or ON_HOLD projects can be archived
                    - Archived projects are hidden from normal views
                    - Tasks under archived projects become read-only
                    - Archived projects can be restored by admin
                    
                    **Valid Status Transitions to ARCHIVED:**
                    - COMPLETED → ARCHIVED
                    - ON_HOLD → ARCHIVED
                    
                    **Authorization:**
                    - Requires ADMIN role
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Project must exist
                    - Current status must be COMPLETED or ON_HOLD
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project archived successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "422", description = "Unprocessable - Invalid status transition")
    })
    @PostMapping("/{projectId}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> archiveProject(
            @Parameter(description = "Project ID to archive", required = true, example = "1")
            @PathVariable Long projectId) {
        projectService.archiveProject(projectId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Transfer project to another team (Admin only)",
            description = """
                    Transfers a project from one team to another.
                    
                    **Business Logic:**
                    - Project is moved to the target team
                    - Project name must be unique in the target team
                    - Cannot transfer to the same team
                    - Task assignments may need to be updated
                    - Project status remains unchanged
                    
                    **Authorization:**
                    - Requires ADMIN role
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Project must exist and not be DELETED
                    - Target team must exist and be ACTIVE
                    - Target team must be different from current team
                    - Project name must be unique in target team
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project transferred successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - same team or null team ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Project or target team not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - Project name exists in target team")
    })
    @PostMapping("/{projectId}/transfer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDto> transferProject(
            @Parameter(description = "Project ID to transfer", required = true, example = "1")
            @PathVariable Long projectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Transfer details with target team ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Transfer Project",
                                    value = """
                                            {
                                                "newTeamId": 2
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody TransferProjectDto transferProjectDto) {
        return ResponseEntity.ok(projectService.transferProject(projectId, transferProjectDto.getNewTeamId()));
    }

    @Operation(
            summary = "Get project by ID",
            description = """
                    Retrieves detailed information about a specific project.
                    
                    **Business Logic:**
                    - Returns project details if accessible
                    - System admins can view all projects (including DELETED)
                    - Team members can only view non-DELETED projects
                    
                    **Authorization:**
                    - Team members can view projects in their team
                    - System ADMIN can view any project
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Project must exist
                    - User must have access to the project's team
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a team member"),
            @ApiResponse(responseCode = "404", description = "Project not found or not accessible")
    })
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> getProjectById(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long projectId) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getProjectById(projectId));
    }

    @Operation(
            summary = "Get projects by owner",
            description = """
                    Retrieves all projects created by a specific user.
                    
                    **Business Logic:**
                    - For regular users: Can only view their own projects (non-DELETED)
                    - For admins: Can view any user's projects (all statuses)
                    
                    **Authorization:**
                    - Users can only query their own projects
                    - System ADMIN can query any user's projects
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Owner ID must not be null
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot view other users' projects")
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<ProjectResponseDto>> getProjectsByOwner(
            @Parameter(description = "Owner user ID", required = true, example = "1")
            @PathVariable Long ownerId,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectsByOwner(pageable, ownerId));
    }

    @Operation(
            summary = "Get projects by team",
            description = """
                    Retrieves all projects belonging to a specific team.
                    
                    **Business Logic:**
                    - For team members: Returns non-DELETED projects only
                    - For system admins: Returns all projects including DELETED
                    - Results are paginated and sorted by creation date (newest first)
                    
                    **Authorization:**
                    - Team members can view projects in their team
                    - System ADMIN can view projects in any team
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Team must exist and be ACTIVE
                    - User must be a member of the team (or admin)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a team member"),
            @ApiResponse(responseCode = "404", description = "Team not found or not active")
    })
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Page<ProjectResponseDto>> getProjectsByTeam(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectsByTeam(pageable, teamId));
    }

    @Operation(
            summary = "Get all projects (Admin only)",
            description = """
                    Retrieves all projects in the system including all statuses.
                    
                    **Business Logic:**
                    - Returns ALL projects regardless of status
                    - Includes ACTIVE, PLANNED, ON_HOLD, COMPLETED, ARCHIVED, DELETED
                    - Results are paginated and sorted by creation date
                    - Useful for system-wide project management and reporting
                    
                    **Authorization:**
                    - Requires ADMIN role
                    - User must have ACTIVE status
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProjectResponseDto>> getAllProjectsForAdmin(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjectsForAdmin(pageable));
    }

    @Operation(
            summary = "Update a project",
            description = """
                    Updates an existing project. Only provided fields will be updated (partial update).
                    
                    **Business Logic:**
                    - At least one field must be provided for update
                    - Project name uniqueness is validated if name is changed
                    - Status transitions are validated against allowed transitions
                    - Date changes are validated (start < end, future dates)
                    - Project must not be DELETED
                    
                    **Allowed Status Transitions:**
                    - PLANNED → ACTIVE, ON_HOLD
                    - ACTIVE → ON_HOLD, COMPLETED
                    - ON_HOLD → ACTIVE, ARCHIVED
                    - COMPLETED → ARCHIVED
                    
                    **Authorization:**
                    - Team OWNER or ADMIN can update projects
                    - System ADMIN can update any project
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Project must exist and not be DELETED
                    - Team must be ACTIVE
                    - At least one field must be provided
                    - Name (if changed): unique in team, 2-100 chars
                    - Status (if changed): valid transition
                    - Dates (if changed): valid future dates
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - validation failed or no fields provided"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not team owner/admin"),
            @ApiResponse(responseCode = "404", description = "Project not found or DELETED"),
            @ApiResponse(responseCode = "409", description = "Conflict - New name already exists in team"),
            @ApiResponse(responseCode = "422", description = "Unprocessable - Invalid status transition")
    })
    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long projectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Project update details - only provided fields will be updated",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Update Name",
                                            value = """
                                                    {
                                                        "name": "New Project Name"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Update Status",
                                            value = """
                                                    {
                                                        "status": "ACTIVE"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Full Update",
                                            value = """
                                                    {
                                                        "name": "Updated Project",
                                                        "description": "New description",
                                                        "status": "ACTIVE",
                                                        "startDate": "2025-02-01T00:00:00Z",
                                                        "endDate": "2025-12-31T23:59:59Z"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody UpdateProjectDto dto) {
        return ResponseEntity.ok(projectService.updateProject(projectId, dto));
    }

    @Operation(
            summary = "Delete a project (Admin only)",
            description = """
                    Soft deletes a project (changes status to DELETED).
                    
                    **Business Logic:**
                    - Performs soft delete - project can be restored by admin
                    - Project status changes to DELETED
                    - Tasks under this project become inaccessible
                    - Project is hidden from normal queries
                    
                    **Authorization:**
                    - Requires ADMIN role
                    - User must have ACTIVE status
                    
                    **Validation:**
                    - Project must exist
                    - Cannot delete already deleted projects
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "422", description = "Unprocessable - Project already deleted")
    })
    @DeleteMapping("/delete/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project ID to delete", required = true, example = "1")
            @PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

}
