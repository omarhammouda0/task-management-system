package com.taskmanagement.comment.dto;

import com.taskmanagement.comment.enums.CommentStatus;

import java.time.Instant;

public record CommentResponseDto(

        Long id,
        String content,
        Long taskId,
        Long userId,
        CommentStatus status,
        Long createdBy,
        Long updatedBy,
        Instant createdAt,
        Instant updatedAt
) {}