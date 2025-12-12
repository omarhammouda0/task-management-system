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
            description = """
                    Creates a new task within a project.
                    
                    **📋 Required Fields:**
                    - `title` (String, max 200 chars) - Task title ✅ REQUIRED
                    - `projectId` (Long) - Project ID ✅ REQUIRED
                    - `description` (String, max 1000 chars) - Description (optional)
                    - `priority` (String) - LOW, MEDIUM, HIGH, URGENT (optional, defaults to MEDIUM)
                    - `status` (String) - TO_DO, IN_PROGRESS, etc. (optional, defaults to TO_DO)
                    - `assignedTo` (Long) - User ID to assign (optional)
                    - `dueDate` (ISO DateTime) - Due date (optional)
                    
                    **Business Logic:**
                    - Task will be created with TO_DO status by default
                    - Priority defaults to MEDIUM if not specified
                    - User must be an active member of the project's team to create tasks
                    - System admins can create tasks in any project
                    - Task title must be unique within the project
                    - Project must be in ACTIVE status
                    
                    **Validations:**
                    - Title: Required, max 200 characters, must be unique within the project
                    - Description: Optional, max 1000 characters
                    - Project ID: Required, must be a positive number
                    - Priority: Optional (LOW, MEDIUM, HIGH, URGENT), defaults to MEDIUM
                    - Assigned To: Optional, if provided user must be active team member
                    - Due Date: Optional, ISO-8601 format
                    
                    **Authorization:**
                    - Authenticated users who are members of the project's team
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data - validation failed or duplicate task title"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not a member of the project's team"),
            @ApiResponse(responseCode = "404", description = "Project not found or assignee user not found")
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
                                                "assignedTo": 5,
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
            description = """
                    Retrieves detailed information about a specific task.
                    
                    **Business Logic:**
                    - User must be a member of the project's team to view the task
                    - System admins can view any task (including deleted ones)
                    - Regular users cannot see deleted tasks
                    - Returns complete task details including assignments and metadata
                    
                    **Authorization:**
                    - Team members of the project containing this task
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not authorized to view this task"),
            @ApiResponse(responseCode = "404", description = "Task not found or deleted")
    })
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @Operation(
            summary = "Get tasks by project",
            description = """
                    Retrieves all tasks belonging to a specific project with pagination support.
                    
                    **Business Logic:**
                    - Returns paginated list of tasks within the project
                    - User must be a member of the project's team
                    - System admins can see all tasks including deleted ones
                    - Regular users only see non-deleted tasks
                    - Project must not be deleted
                    - Supports sorting by any task field (e.g., priority, dueDate, status)
                    
                    **Pagination:**
                    - Default page size: 20
                    - Page numbers start from 0
                    - Sort example: ?sort=priority,desc&sort=dueDate,asc
                    
                    **Authorization:**
                    - Team members of the project
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not a member of the project's team"),
            @ApiResponse(responseCode = "404", description = "Project not found or deleted")
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
            description = """
                    Updates an existing task. Only provided fields will be updated (partial update supported).
                    
                    **📋 Optional Fields (all fields are optional - partial update):**
                    - `title` (String, max 200 chars) - New task title (optional)
                    - `description` (String, max 1000 chars) - New description (optional)
                    - `status` (String) - TO_DO, IN_PROGRESS, IN_REVIEW, DONE, BLOCKED (optional)
                    - `priority` (String) - LOW, MEDIUM, HIGH, URGENT (optional)
                    - `assignedTo` (Long) - User ID to assign (optional, null to unassign)
                    - `dueDate` (ISO DateTime) - Due date (optional)
                    
                    **Business Logic:**
                    - Partial updates supported - only send fields you want to change
                    - At least one field must be provided
                    - Task title must remain unique within the project if changed
                    - Status transitions are validated (see status transition rules below)
                    - Completed timestamp is automatically set when status changes to DONE
                    - Cannot update deleted tasks
                    
                    **Status Transition Rules:**
                    - TO_DO → IN_PROGRESS, BLOCKED
                    - IN_PROGRESS → IN_REVIEW, DONE, BLOCKED, TO_DO
                    - IN_REVIEW → DONE, IN_PROGRESS, BLOCKED
                    - DONE → TO_DO, IN_PROGRESS (reopening task)
                    - BLOCKED → TO_DO, IN_PROGRESS (unblocking task)
                    - DELETED status cannot be changed (use delete endpoint)
                    
                    **Validations:**
                    - Title: Optional, max 200 characters, must be unique within project if provided
                    - Description: Optional, max 1000 characters
                    - Status: Optional, must follow transition rules
                    - Priority: Optional (LOW, MEDIUM, HIGH, URGENT)
                    - Due Date: Optional, ISO-8601 format
                    
                    **Authorization:**
                    - Team owners and admins of the project
                    - Task assignee (if task is assigned to them)
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data - validation failed, duplicate title, or invalid status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user not allowed to update this task"),
            @ApiResponse(responseCode = "404", description = "Task not found or deleted")
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
            description = """
                    Soft deletes a task by setting its status to DELETED.
                    
                    **Business Logic:**
                    - Performs soft delete (status changed to DELETED)
                    - Task data is preserved in the database
                    - Deleted tasks are hidden from regular users
                    - System admins can still view deleted tasks
                    - Cannot delete an already deleted task
                    
                    **Authorization:**
                    - Team owners and admins of the project
                    - System admins (ADMIN role)
                    - Note: Regular team members and task assignees CANNOT delete tasks
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - task is already deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only team owner/admin or system admin can delete tasks"),
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
            description = """
                    Assigns a task to a specific user within the project's team.
                    
                    **📋 Required Fields:**
                    - `userId` (Long) - ID of the user to assign ✅ REQUIRED
                    
                    **Business Logic:**
                    - Assignee must be an active member of the project's team
                    - Can reassign already assigned tasks
                    - Task cannot be deleted
                    - Assignee must have ACTIVE user status
                    
                    **Authorization:**
                    - Team owners and admins can assign tasks to any team member
                    - Regular team members can only assign tasks to themselves
                    - System admins can assign tasks to anyone
                    
                    **Validations:**
                    - User ID: Required, must be a positive number
                    - User must exist and be active
                    - User must be a member of the project's team
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task assigned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or user not in team"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - can only assign to yourself unless you're team owner/admin"),
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
                                                "userId": 5
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
            description = """
                    Removes the current assignee from the task.
                    
                    **Business Logic:**
                    - Removes the assigned user from the task
                    - Task must currently be assigned to someone
                    - Task cannot be deleted
                    
                    **Authorization:**
                    - Team owners and admins of the project
                    - Task assignee (can unassign themselves)
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task unassigned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - task is already unassigned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user not allowed to unassign this task"),
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
            description = """
                    Retrieves all tasks assigned to the currently authenticated user.
                    
                    **Business Logic:**
                    - Returns only tasks assigned to the current user
                    - Excludes deleted tasks
                    - Supports pagination and sorting
                    - User must be active
                    - Returns tasks from all projects where user is assigned
                    
                    **Pagination:**
                    - Default page size: 20
                    - Page numbers start from 0
                    - Sort example: ?sort=dueDate,asc&sort=priority,desc
                    
                    **Authorization:**
                    - Any authenticated active user
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user account is not active")
    })
    @GetMapping("/my-tasks")
    public ResponseEntity<Page<TaskResponseDto>> getMyTasks(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getMyTasks(pageable));
    }

    @Operation(
            summary = "Get all tasks (Admin)",
            description = """
                    Retrieves all tasks in the system. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Returns all tasks from all projects
                    - Includes deleted tasks
                    - Supports pagination and sorting
                    - Only accessible by system administrators
                    
                    **Pagination:**
                    - Default page size: 20
                    - Page numbers start from 0
                    - Sort example: ?sort=createdAt,desc
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TaskResponseDto>> getAllTasksForAdmin(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getAllTasksForAdmin(pageable));
    }
}
