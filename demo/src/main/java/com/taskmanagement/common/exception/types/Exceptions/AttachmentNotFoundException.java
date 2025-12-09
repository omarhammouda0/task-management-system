package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.NotFoundException;

public class AttachmentNotFoundException extends NotFoundException {
    public AttachmentNotFoundException(Long attachmentId) {

        super( ErrorCode.ATTACHMENT_NOT_FOUND.name ( ) ,
                "Attachment not found with ID: " + attachmentId);
    }


}