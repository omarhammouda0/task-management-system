package com.taskmanagement.attachment.controller;

import com.taskmanagement.attachment.dto.AttachmentResponseDto;
import com.taskmanagement.attachment.service.AttachmentService;
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
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/task/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponseDto> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(attachmentService.uploadAttachment(taskId, file));
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponseDto> getAttachmentById(
            @PathVariable Long attachmentId) {
        return ResponseEntity.ok(attachmentService.getAttachmentById(attachmentId));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<Page<AttachmentResponseDto>> getAttachmentsByTask(
            @PathVariable Long taskId,
            Pageable pageable) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByTask(taskId, pageable));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(
            @PathVariable Long attachmentId) {
        return attachmentService.downloadAttachment(attachmentId);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-attachments")
    public ResponseEntity<Page<AttachmentResponseDto>> getMyAttachments(Pageable pageable) {
        return ResponseEntity.ok(attachmentService.getMyAttachments(pageable));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AttachmentResponseDto>> getAllAttachmentsForAdmin(
            Pageable pageable) {
        return ResponseEntity.ok(attachmentService.getAllAttachmentsForAdmin(pageable));
    }
}