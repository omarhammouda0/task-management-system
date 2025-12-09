package com.taskmanagement.attachment.mapper;

import com.taskmanagement.attachment.dto.AttachmentResponseDto;
import com.taskmanagement.attachment.entity.Attachment;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentResponseDto toDto(Attachment attachment) {
        if (attachment == null) {
            return null;
        }

        String downloadUrl = "/api/attachments/" + attachment.getId() + "/download";

        return new AttachmentResponseDto(

                attachment.getId(),
                attachment.getOriginalFilename(),
                attachment.getStoredFilename(),
                attachment.getFileSize(),
                attachment.getContentType(),
                attachment.getTaskId(),
                attachment.getUserId(),
                attachment.getStatus(),
                downloadUrl,
                attachment.getCreatedBy(),
                attachment.getUpdatedBy(),
                attachment.getCreatedAt(),
                attachment.getUpdatedAt()
        );
    }
}