package com.taskmanagement.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCommentDto(

        @NotNull(message = "Task ID is required")
        @Positive(message = "Task ID must be positive")
        Long taskId,

        @NotBlank(message = "Comment content cannot be blank")
        @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
        String content
) {}