package com.taskmanagement.comment.dto;

import com.taskmanagement.comment.enums.CommentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response containing comment details")
public record CommentResponseDto(

        @Schema(description = "Unique comment ID", example = "1")
        Long id,

        @Schema(description = "Comment content text", example = "This task needs more clarification.")
        String content,

        @Schema(description = "ID of the task this comment belongs to", example = "1")
        Long taskId,

        @Schema(description = "ID of the user who authored the comment", example = "1")
        Long userId,

        @Schema(description = "Comment status", example = "ACTIVE")
        CommentStatus status,

        @Schema(description = "ID of user who created this comment", example = "1")
        Long createdBy,

        @Schema(description = "ID of user who last updated this comment", example = "1")
        Long updatedBy,

        @Schema(description = "Timestamp when comment was created", example = "2025-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Timestamp when comment was last updated", example = "2025-01-15T10:30:00Z")
        Instant updatedAt
) {}