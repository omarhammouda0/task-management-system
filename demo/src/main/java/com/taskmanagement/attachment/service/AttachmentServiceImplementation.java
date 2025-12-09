package com.taskmanagement.attachment.service;

import com.taskmanagement.attachment.dto.AttachmentResponseDto;
import com.taskmanagement.attachment.entity.Attachment;
import com.taskmanagement.attachment.enums.AttachmentStatus;
import com.taskmanagement.attachment.mapper.AttachmentMapper;
import com.taskmanagement.attachment.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class AttachmentServiceImplementation implements AttachmentService {

    private final SecurityHelper securityHelper;
    private final AttachmentMapper attachmentMapper;
    private final AttachmentRepository attachmentRepository;
    private final MinioService minioService;

    @Value("${attachment.max-file-size}")
    private long maxFileSize;

    @Value("${attachment.max-files-per-task}")
    private int maxFilesPerTask;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    @Transactional
    public AttachmentResponseDto uploadAttachment(Long taskId, MultipartFile file) {

        Objects.requireNonNull(taskId, "Task ID must not be null");
        Objects.requireNonNull(file, "File must not be null");

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var task = securityHelper.taskExistsAndNotDeletedCheck(taskId);

        securityHelper.canUploadToTask(currentUser, task);

        validateFileSize(file.getSize());

        securityHelper.validateMaxFilesPerTask(taskId, maxFilesPerTask);

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("File must have a name");
        }

        String storedFilename = minioService.generateStoredFilename(originalFilename);

        String objectKey = minioService.uploadFile(file, storedFilename);

        var attachment = Attachment.builder()

                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .bucketName(bucketName)
                .objectKey(objectKey)
                .fileSize(file.getSize())
                .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .task(task)
                .taskId(taskId)
                .user(currentUser)
                .userId(currentUser.getId())
                .status(AttachmentStatus.ACTIVE)

                .build();

        attachment.setCreatedBy(currentUser.getId());

        var savedAttachment = attachmentRepository.save(attachment);

        log.info("Attachment '{}' (ID: {}) uploaded to task {} by user {} (ID: {})",
                originalFilename,
                savedAttachment.getId(),
                taskId,
                currentUser.getEmail(),
                currentUser.getId());

        return attachmentMapper.toDto(savedAttachment);
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponseDto getAttachmentById(Long attachmentId) {
        Objects.requireNonNull(attachmentId, "Attachment ID must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var attachment = securityHelper.attachmentExistsAndNotDeletedCheck(attachmentId);

        securityHelper.canAccessAttachment(currentUser, attachment);

        return attachmentMapper.toDto(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttachmentResponseDto> getAttachmentsByTask(Long taskId, Pageable pageable) {

        Objects.requireNonNull(taskId, "Task ID must not be null");
        Objects.requireNonNull(pageable, "Pageable must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var task = securityHelper.taskExistsAndNotDeletedCheck(taskId);

        securityHelper.canAccessTask(currentUser, task);

        if (securityHelper.isSystemAdmin(currentUser)) {
            return attachmentRepository.findByTaskId(taskId, pageable)
                    .map(attachmentMapper::toDto);
        } else {
            return attachmentRepository.findByTaskIdAndNotDeleted(taskId, pageable)
                    .map(attachmentMapper::toDto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<InputStreamResource> downloadAttachment(Long attachmentId) {
        Objects.requireNonNull(attachmentId, "Attachment ID must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var attachment = securityHelper.attachmentExistsAndNotDeletedCheck(attachmentId);

        securityHelper.canAccessAttachment(currentUser, attachment);

        InputStream fileStream = minioService.downloadFile(attachment.getObjectKey());

        log.info("Attachment '{}' (ID: {}) downloaded by user {} (ID: {})",
                attachment.getOriginalFilename(),
                attachment.getId(),
                currentUser.getEmail(),
                currentUser.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                .body(new InputStreamResource(fileStream));
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Objects.requireNonNull(attachmentId, "Attachment ID must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var attachment = securityHelper.attachmentExistsAndNotDeletedCheck(attachmentId);

        securityHelper.canDeleteAttachment(currentUser, attachment);

        if (attachment.getStatus() == AttachmentStatus.DELETED) {
            throw new IllegalStateException("Attachment is already deleted");
        }

        attachment.setStatus(AttachmentStatus.DELETED);
        attachment.setUpdatedBy(currentUser.getId());

        attachmentRepository.save(attachment);

        log.info("Attachment '{}' (ID: {}) soft-deleted by user {} (ID: {})",
                attachment.getOriginalFilename(),
                attachment.getId(),
                currentUser.getEmail(),
                currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttachmentResponseDto> getMyAttachments(Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        return attachmentRepository.findByCreatedByAndNotDeleted(currentUser.getId(), pageable)
                .map(attachmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttachmentResponseDto> getAllAttachmentsForAdmin(Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        securityHelper.systemAdminCheck(currentUser);

        return attachmentRepository.findAll(pageable)
                .map(attachmentMapper::toDto);
    }

    private void validateFileSize(long fileSize) {
        if (fileSize > maxFileSize) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed: " + (maxFileSize / 1024 / 1024) + " MB"
            );
        }
    }
}