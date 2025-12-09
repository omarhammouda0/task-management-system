package com.taskmanagement.attachment.service;

import com.taskmanagement.attachment.dto.AttachmentResponseDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {

    AttachmentResponseDto uploadAttachment(Long taskId, MultipartFile file);

    AttachmentResponseDto getAttachmentById(Long attachmentId);

    Page<AttachmentResponseDto> getAttachmentsByTask(Long taskId, Pageable pageable);

    ResponseEntity<InputStreamResource> downloadAttachment(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    Page<AttachmentResponseDto> getMyAttachments(Pageable pageable);

    Page<AttachmentResponseDto> getAllAttachmentsForAdmin(Pageable pageable);
}