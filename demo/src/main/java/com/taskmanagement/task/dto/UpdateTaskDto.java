package com.taskmanagement.task.dto;

import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request body for updating a task. All fields are optional - only provided fields will be updated.")
public record UpdateTaskDto(

        @Schema(description = "New task title", example = "Updated task title",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 200)
        @Size(max = 200, message = "Task title must not exceed 200 characters")
        String title,

        @Schema(description = "New task description", example = "Updated task description with more details",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 1000)
        @Size(max = 1000, message = "Task description must not exceed 1000 characters")
        String description,

        @Schema(description = "New task status (must follow valid transitions: TO_DO→IN_PROGRESS→IN_REVIEW→DONE)",
                example = "IN_PROGRESS", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"TO_DO", "IN_PROGRESS", "IN_REVIEW", "DONE", "BLOCKED"})
        TaskStatus status,

        @Schema(description = "New priority level", example = "HIGH",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
        TaskPriority priority,

        @Schema(description = "ID of user to assign task to (must be team member, use null to unassign)",
                example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long assignedTo,

        @Schema(description = "New due date (ISO 8601 format)",
                example = "2025-02-28T23:59:59Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Instant dueDate
) {

}