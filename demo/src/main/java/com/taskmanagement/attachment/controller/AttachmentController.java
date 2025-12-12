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
            description = """
                    Uploads a file attachment to a specific task.
                    
                    **📋 Required Fields:**
                    - `taskId` (Path) - ID of the task ✅ REQUIRED
                    - `file` (MultipartFile) - File to upload (max 10MB) ✅ REQUIRED
                    
                    **Business Logic:**
                    - File is stored in MinIO object storage
                    - Generates unique filename to prevent conflicts
                    - User must be a member of the task's project team
                    - System admins can upload to any task
                    - Task must not be deleted
                    - Maximum number of attachments per task is enforced
                    - Attachment is created with ACTIVE status
                    
                    **File Constraints:**
                    - Maximum file size: 10MB (configurable)
                    - Maximum attachments per task: 20 (configurable)
                    - Supported formats: All file types allowed
                    - File must have a valid name
                    - Empty files are rejected
                    
                    **Validations:**
                    - Task ID: Required, must be a positive number
                    - File: Required, not empty, size within limit
                    - Task must exist and not be deleted
                    - User must be an active team member
                    - Maximum files per task limit not exceeded
                    
                    **Authorization:**
                    - Team members of the project containing the task
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attachment uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttachmentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file - empty, too large, or max files limit reached"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not a member of the project's team"),
            @ApiResponse(responseCode = "404", description = "Task not found or deleted"),
            @ApiResponse(responseCode = "413", description = "Payload too large - file size exceeds 10MB limit")
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
            description = """
                    Retrieves metadata information about a specific attachment.
                    
                    **Business Logic:**
                    - Returns attachment metadata (not the file content)
                    - Metadata includes: filename, size, type, uploader, timestamps
                    - User must be a member of the project's team
                    - System admins can view any attachment metadata
                    - Regular users cannot see deleted attachments
                    - Use the download endpoint to get the actual file
                    
                    **Authorization:**
                    - Team members of the project containing the task
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment metadata found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttachmentResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not authorized to view this attachment"),
            @ApiResponse(responseCode = "404", description = "Attachment not found or deleted")
    })
    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponseDto> getAttachmentById(
            @Parameter(description = "Attachment ID", required = true, example = "1")
            @PathVariable Long attachmentId) {
        return ResponseEntity.ok(attachmentService.getAttachmentById(attachmentId));
    }

    @Operation(
            summary = "Get attachments by task",
            description = """
                    Retrieves all attachments for a specific task with pagination support.
                    
                    **Business Logic:**
                    - Returns paginated list of attachment metadata for the task
                    - Attachments are ordered by upload time (newest first by default)
                    - User must be a member of the project's team
                    - System admins can see all attachments including deleted ones
                    - Regular users only see active attachments
                    - Task must not be deleted
                    - Does not return file content, only metadata
                    
                    **Pagination:**
                    - Default page size: 20
                    - Page numbers start from 0
                    - Sort example: ?sort=createdAt,desc&sort=fileSize,asc
                    
                    **Authorization:**
                    - Team members of the project containing the task
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not a member of the project's team"),
            @ApiResponse(responseCode = "404", description = "Task not found or deleted")
    })
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Page<AttachmentResponseDto>> getAttachmentsByTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByTask(taskId, pageable));
    }

    @Operation(
            summary = "Download attachment",
            description = """
                    Downloads the actual file content of an attachment.
                    
                    **Business Logic:**
                    - Returns the actual file as a binary stream
                    - File is retrieved from MinIO object storage
                    - Sets appropriate Content-Type header based on file type
                    - Sets Content-Disposition header for browser download
                    - Original filename is preserved in the download
                    - User must be a member of the project's team
                    - Attachment must not be deleted
                    - Download activity is logged
                    
                    **Response Headers:**
                    - Content-Type: Based on file type (e.g., image/png, application/pdf)
                    - Content-Disposition: attachment; filename="original_filename.ext"
                    
                    **Authorization:**
                    - Team members of the project containing the task
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File download started",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not authorized to download this file"),
            @ApiResponse(responseCode = "404", description = "Attachment not found or deleted"),
            @ApiResponse(responseCode = "500", description = "Internal server error - file retrieval failed")
    })
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(
            @Parameter(description = "Attachment ID", required = true, example = "1")
            @PathVariable Long attachmentId) {
        return attachmentService.downloadAttachment(attachmentId);
    }

    @Operation(
            summary = "Delete attachment",
            description = """
                    Soft deletes an attachment by setting its status to DELETED.
                    
                    **Business Logic:**
                    - Performs soft delete (status changed to DELETED)
                    - Attachment metadata is preserved in the database
                    - File remains in object storage (not physically deleted)
                    - Deleted attachments are hidden from regular users
                    - System admins can still view deleted attachments
                    - Cannot delete an already deleted attachment
                    
                    **Authorization:**
                    - File uploader (the user who uploaded the attachment)
                    - Team owners and admins of the project
                    - System admins (ADMIN role)
                    - Note: Regular team members cannot delete other members' attachments
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - attachment is already deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only uploader, team owner/admin, or system admin can delete"),
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
            description = """
                    Retrieves all attachments uploaded by the currently authenticated user.
                    
                    **Business Logic:**
                    - Returns only attachments uploaded by the current user
                    - Excludes deleted attachments
                    - Supports pagination and sorting
                    - User must be active
                    - Returns attachments from all tasks across all projects
                    - Returns metadata only, not file content
                    
                    **Pagination:**
                    - Default page size: 20
                    - Page numbers start from 0
                    - Sort example: ?sort=createdAt,desc&sort=fileSize,desc
                    
                    **Authorization:**
                    - Any authenticated active user
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user account is not active")
    })
    @GetMapping("/my-attachments")
    public ResponseEntity<Page<AttachmentResponseDto>> getMyAttachments(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(attachmentService.getMyAttachments(pageable));
    }

    @Operation(
            summary = "Get all attachments (Admin)",
            description = """
                    Retrieves all attachments in the system. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Returns all attachments from all tasks and projects
                    - Includes deleted attachments
                    - Supports pagination and sorting
                    - Only accessible by system administrators
                    - Returns metadata only, not file content
                    
                    **Pagination:**
                    - Default page size: 20
                    - Page numbers start from 0
                    - Sort example: ?sort=createdAt,desc&sort=fileSize,desc
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AttachmentResponseDto>> getAllAttachmentsForAdmin(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(attachmentService.getAllAttachmentsForAdmin(pageable));
    }
}