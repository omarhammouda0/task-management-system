package com.taskmanagement.task.dto;

import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request body for creating a new task")
public record CreateTaskDto(

        @Schema(description = "Task title (must be unique within the project)", example = "Implement user login",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 200)
        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Task title must not exceed 200 characters")
        String title,

        @Schema(description = "Detailed task description", example = "Implement OAuth2 login with Google and GitHub providers",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 1000)
        @Size (max = 1000, message = "Task description must not exceed 1000 characters")
        String description,

        @Schema(description = "ID of the project this task belongs to (must be ACTIVE project)", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Project ID is required")
        @Positive (message = "Project ID must be a positive number")
        Long projectId,

        @Schema(description = "Task priority level (defaults to MEDIUM if not provided)",
                example = "HIGH", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
        TaskPriority priority,

        @Schema(description = "Initial task status (defaults to TO_DO if not provided)",
                example = "TO_DO", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"TO_DO", "IN_PROGRESS", "IN_REVIEW", "DONE", "BLOCKED"})
        TaskStatus status ,

        @Schema(description = "ID of the user to assign the task to (must be a team member)",
                example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long assignedTo,

        @Schema(description = "Task due date (ISO 8601 format)",
                example = "2025-01-31T23:59:59Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Instant dueDate
) {
        public CreateTaskDto {

                if (status == null)
                        status = TaskStatus.TO_DO ;
        }



}