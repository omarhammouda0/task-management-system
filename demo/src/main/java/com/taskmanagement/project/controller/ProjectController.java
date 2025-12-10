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
@Tag(name = "Projects", description = "Project management endpoints - create, update, archive, and manage projects")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "Create a new project",
            description = "Creates a new project associated with a team. The authenticated user becomes the project owner."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Project creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Create Project",
                                    value = """
                                            {
                                                "name": "E-Commerce Platform",
                                                "description": "Online shopping platform development",
                                                "teamId": 1,
                                                "startDate": "2025-01-01",
                                                "endDate": "2025-06-30"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CreateProjectDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(dto));
    }

    @Operation(
            summary = "Restore a deleted project (Admin)",
            description = "Restores a previously deleted/archived project. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project restored successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PostMapping("/{projectId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDto> restoreProject(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long projectId) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.restoreProject(projectId));
    }

    @Operation(
            summary = "Activate a project (Admin)",
            description = "Activates an inactive project. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project activated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PostMapping("/{projectId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDto> activateProject(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long projectId) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.activateProject(projectId));
    }

    @Operation(
            summary = "Archive a project (Admin)",
            description = "Archives a project. Archived projects are hidden from normal views. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project archived successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PostMapping("/{projectId}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> archiveProject(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long projectId) {
        projectService.archiveProject(projectId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Transfer project to another team (Admin)",
            description = "Transfers project ownership to a different team. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Project or team not found")
    })
    @PostMapping("/{projectId}/transfer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDto> transferProject(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long projectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Transfer details",
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
            description = "Retrieves detailed information about a specific project"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> getProjectById(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long projectId) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getProjectById(projectId));
    }

    @Operation(
            summary = "Get projects by owner",
            description = "Retrieves all projects owned by a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<ProjectResponseDto>> getProjectsByOwner(
            @Parameter(description = "Owner user ID", required = true, example = "1")
            @PathVariable Long ownerId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectsByOwner(pageable, ownerId));
    }

    @Operation(
            summary = "Get projects by team",
            description = "Retrieves all projects belonging to a specific team"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Page<ProjectResponseDto>> getProjectsByTeam(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectsByTeam(pageable, teamId));
    }

    @Operation(
            summary = "Get all projects (Admin)",
            description = "Retrieves all projects in the system including archived ones. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProjectResponseDto>> getAllProjectsForAdmin(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjectsForAdmin(pageable));
    }

    @Operation(
            summary = "Update a project",
            description = "Updates an existing project. Only provided fields will be updated."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to update this project"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long projectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Project update details",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Update Project",
                                    value = """
                                            {
                                                "name": "Updated Project Name",
                                                "description": "Updated description",
                                                "status": "IN_PROGRESS"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UpdateProjectDto dto) {
        return ResponseEntity.ok(projectService.updateProject(projectId, dto));
    }

    @Operation(
            summary = "Delete a project (Admin)",
            description = "Permanently deletes a project and all associated data. **Requires ADMIN role. This action cannot be undone.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @DeleteMapping("/delete/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

}
