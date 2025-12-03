package com.taskmanagement.task.dto;

import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateTaskDto(

        @Size(max = 200, message = "Task title must not exceed 200 characters")
        String title,

        @Size(max = 1000, message = "Task description must not exceed 1000 characters")
        String description,

        TaskStatus status,

        TaskPriority priority,

        Long assignedTo,

        Instant dueDate
) {

}