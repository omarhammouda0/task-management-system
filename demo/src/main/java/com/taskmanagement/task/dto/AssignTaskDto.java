package com.taskmanagement.task.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignTaskDto(

        @NotNull(message = "User ID is required")
        @Positive (message = "User ID must be a positive number")
        Long userId
) {
}