package com.taskmanagement.attachment.dto;

import com.taskmanagement.attachment.enums.AttachmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response containing attachment details")
public record AttachmentResponseDto(

        @Schema(description = "Unique attachment ID", example = "1")
        Long id,

        @Schema(description = "Original filename as uploaded", example = "document.pdf")
        String originalFilename,

        @Schema(description = "Stored filename in storage system", example = "abc123-document.pdf")
        String storedFilename,

        @Schema(description = "File size in bytes", example = "1048576")
        Long fileSize,

        @Schema(description = "MIME content type", example = "application/pdf")
        String contentType,

        @Schema(description = "ID of the task this attachment belongs to", example = "1")
        Long taskId,

        @Schema(description = "ID of the user who uploaded the attachment", example = "1")
        Long userId,

        @Schema(description = "Attachment status", example = "ACTIVE")
        AttachmentStatus status,

        @Schema(description = "URL to download the attachment", example = "/api/attachments/1/download")
        String downloadUrl,

        @Schema(description = "ID of user who created this record", example = "1")
        Long createdBy,

        @Schema(description = "ID of user who last updated this record", example = "1")
        Long updatedBy,

        @Schema(description = "Timestamp when attachment was created", example = "2025-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Timestamp when attachment was last updated", example = "2025-01-15T10:30:00Z")
        Instant updatedAt

)
{}