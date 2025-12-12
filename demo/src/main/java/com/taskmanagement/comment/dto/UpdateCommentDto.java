package com.taskmanagement.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating a comment (only the comment author can update)")
public record UpdateCommentDto(
        @Schema(description = "New comment content", example = "Updated comment with more details.",
                requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 2000)
        @NotBlank(message = "Comment content cannot be blank")
        @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
        String content
) {}