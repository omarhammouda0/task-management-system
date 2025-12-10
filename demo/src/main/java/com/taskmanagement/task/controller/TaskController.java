package com.taskmanagement.task.controller;

import com.taskmanagement.task.dto.AssignTaskDto;
import com.taskmanagement.task.dto.CreateTaskDto;
import com.taskmanagement.task.dto.TaskResponseDto;
import com.taskmanagement.task.dto.UpdateTaskDto;
import com.taskmanagement.task.service.TaskService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management endpoints - create, update, assign, and manage tasks within projects")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Create a new task",
            description = "Creates a new task within a project. The task will be created with PENDING status by default."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Task creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Create Task",
                                    value = """
                                            {
                                                "title": "Implement user authentication",
                                                "description": "Add JWT-based authentication to the API",
                                                "projectId": 1,
                                                "priority": "HIGH",
                                                "dueDate": "2025-12-31T23:59:59Z"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CreateTaskDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(dto));
    }

    @Operation(
            summary = "Get task by ID",
            description = "Retrieves detailed information about a specific task"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @Operation(
            summary = "Get tasks by project",
            description = "Retrieves all tasks belonging to a specific project with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<TaskResponseDto>> getTasksByProject(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, pageable));
    }

    @Operation(
            summary = "Update a task",
            description = "Updates an existing task. Only provided fields will be updated (partial update supported)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to update this task"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Task update details (partial update)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Update Task",
                                    value = """
                                            {
                                                "title": "Updated task title",
                                                "status": "IN_PROGRESS",
                                                "priority": "URGENT"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UpdateTaskDto dto) {
        return ResponseEntity.ok(taskService.updateTask(taskId, dto));
    }

    @Operation(
            summary = "Delete a task",
            description = "Permanently deletes a task. This action cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to delete this task"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Assign task to user",
            description = "Assigns a task to a specific user. The user must be a member of the project's team."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task assigned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or user not in team"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task or user not found")
    })
    @PostMapping("/{taskId}/assign")
    public ResponseEntity<TaskResponseDto> assignTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Assignment details",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Assign Task",
                                    value = """
                                            {
                                                "assigneeId": 5
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody AssignTaskDto dto) {
        return ResponseEntity.ok(taskService.assignTask(taskId, dto));
    }

    @Operation(
            summary = "Unassign task",
            description = "Removes the current assignee from the task"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task unassigned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PostMapping("/{taskId}/unassign")
    public ResponseEntity<TaskResponseDto> unassignTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.unassignTask(taskId));
    }

    @Operation(
            summary = "Get my tasks",
            description = "Retrieves all tasks assigned to the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-tasks")
    public ResponseEntity<Page<TaskResponseDto>> getMyTasks(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getMyTasks(pageable));
    }

    @Operation(
            summary = "Get all tasks (Admin)",
            description = "Retrieves all tasks in the system. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TaskResponseDto>> getAllTasksForAdmin(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getAllTasksForAdmin(pageable));
    }
}
