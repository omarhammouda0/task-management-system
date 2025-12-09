package com.taskmanagement.task.dto;

import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateTaskDto(

        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Task title must not exceed 200 characters")
        String title,

        @Size (max = 1000, message = "Task description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Project ID is required")
        @Positive (message = "Project ID must be a positive number")
        Long projectId,

        TaskPriority priority,

        TaskStatus status ,

        Long assignedTo,

        Instant dueDate
) {
        public CreateTaskDto {

                if (status == null)
                        status = TaskStatus.TO_DO ;
        }



}