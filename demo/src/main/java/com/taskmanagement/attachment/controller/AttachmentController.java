package com.taskmanagement.attachment.controller;

import com.taskmanagement.attachment.dto.AttachmentResponseDto;
import com.taskmanagement.attachment.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/attachments")
@Tag(name = "Attachments", description = "File attachment management - upload, download, and manage task attachments")
@SecurityRequirement(name = "bearerAuth")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(
            summary = "Upload attachment to task",
            description = "Uploads a file attachment to a specific task. Supported formats include images, documents, and archives. Maximum file size is 10MB."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attachment uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttachmentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "413", description = "File size exceeds limit")
    })
    @PostMapping(value = "/task/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponseDto> uploadAttachment(
            @Parameter(description = "Task ID to attach file to", required = true, example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "File to upload (max 10MB)", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(attachmentService.uploadAttachment(taskId, file));
    }

    @Operation(
            summary = "Get attachment metadata by ID",
            description = "Retrieves metadata information about a specific attachment (filename, size, type, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment metadata found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttachmentResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponseDto> getAttachmentById(
            @Parameter(description = "Attachment ID", required = true, example = "1")
            @PathVariable Long attachmentId) {
        return ResponseEntity.ok(attachmentService.getAttachmentById(attachmentId));
    }

    @Operation(
            summary = "Get attachments by task",
            description = "Retrieves all attachments for a specific task with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Page<AttachmentResponseDto>> getAttachmentsByTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByTask(taskId, pageable));
    }

    @Operation(
            summary = "Download attachment",
            description = "Downloads the actual file content of an attachment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File download started",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(
            @Parameter(description = "Attachment ID", required = true, example = "1")
            @PathVariable Long attachmentId) {
        return attachmentService.downloadAttachment(attachmentId);
    }

    @Operation(
            summary = "Delete attachment",
            description = "Permanently deletes an attachment. Only the uploader or admin can delete."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to delete this attachment"),
            @ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "Attachment ID", required = true, example = "1")
            @PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get my attachments",
            description = "Retrieves all attachments uploaded by the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-attachments")
    public ResponseEntity<Page<AttachmentResponseDto>> getMyAttachments(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.ok(attachmentService.getMyAttachments(pageable));
    }

    @Operation(
            summary = "Get all attachments (Admin)",
            description = "Retrieves all attachments in the system. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AttachmentResponseDto>> getAllAttachmentsForAdmin(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.ok(attachmentService.getAllAttachmentsForAdmin(pageable));
    }
}