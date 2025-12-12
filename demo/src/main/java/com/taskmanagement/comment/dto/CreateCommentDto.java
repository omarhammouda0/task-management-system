 package com.taskmanagement.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a new comment on a task")
public record CreateCommentDto(

        @Schema(description = "ID of the task to comment on (must be a member of the task's project team)",
                example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Task ID is required")
        @Positive(message = "Task ID must be positive")
        Long taskId,

        @Schema(description = "Comment content", example = "This task needs more clarification on the requirements.",
                requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 2000)
        @NotBlank(message = "Comment content cannot be blank")
        @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
        String content
) {}