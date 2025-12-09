package com.taskmanagement.attachment.dto;

import com.taskmanagement.attachment.enums.AttachmentStatus;

import java.time.Instant;

public record AttachmentResponseDto(

        Long id,
        String originalFilename,
        String storedFilename,
        Long fileSize,
        String contentType,
        Long taskId,
        Long userId,
        AttachmentStatus status,
        String downloadUrl,
        Long createdBy,
        Long updatedBy,
        Instant createdAt,
        Instant updatedAt

)
{}