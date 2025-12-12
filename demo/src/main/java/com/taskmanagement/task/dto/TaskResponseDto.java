package com.taskmanagement.task.dto;

import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response containing task details")
public record TaskResponseDto(

        @Schema(description = "Unique task ID", example = "1")
        Long id,

        @Schema(description = "Task title", example = "Implement user authentication")
        String title,

        @Schema(description = "Detailed task description", example = "Implement OAuth2 login with Google and GitHub")
        String description,

        @Schema(description = "Current task status", example = "IN_PROGRESS")
        TaskStatus status,

        @Schema(description = "Task priority level", example = "HIGH")
        TaskPriority priority,

        @Schema(description = "ID of the project this task belongs to", example = "1")
        Long projectId,

        @Schema(description = "ID of the user assigned to this task (null if unassigned)", example = "1")
        Long assignedTo,

        @Schema(description = "Task due date", example = "2025-01-31T23:59:59Z")
        Instant dueDate,

        @Schema(description = "Timestamp when task was marked as DONE", example = "2025-01-20T15:30:00Z")
        Instant completedAt,

        @Schema(description = "ID of user who created this task", example = "1")
        Long createdBy,

        @Schema(description = "ID of user who last updated this task", example = "1")
        Long updatedBy,

        @Schema(description = "Timestamp when task was created", example = "2025-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Timestamp when task was last updated", example = "2025-01-15T10:30:00Z")
        Instant updatedAt
) {
}