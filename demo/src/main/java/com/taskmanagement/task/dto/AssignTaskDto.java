package com.taskmanagement.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for assigning a task to a user")
public record AssignTaskDto(

        @Schema(description = "ID of the user to assign the task to (must be a member of the project's team)",
                example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "User ID is required")
        @Positive (message = "User ID must be a positive number")
        Long userId
) {
}