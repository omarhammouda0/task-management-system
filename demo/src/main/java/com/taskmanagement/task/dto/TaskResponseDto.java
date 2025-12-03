package com.taskmanagement.task.dto;

import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;

import java.time.Instant;

public record TaskResponseDto(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Long projectId,
        Long assignedTo,
        Instant dueDate,
        Instant completedAt,
        Long createdBy,
        Long updatedBy,
        Instant createdAt,
        Instant updatedAt
) {
}