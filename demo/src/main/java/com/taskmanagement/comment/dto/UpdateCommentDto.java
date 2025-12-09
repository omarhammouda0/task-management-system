package com.taskmanagement.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentDto(
        @NotBlank(message = "Comment content cannot be blank")
        @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
        String content
) {}