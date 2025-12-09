package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.NotFoundException;

public class CommentNotFoundException extends NotFoundException {

    public CommentNotFoundException(Long commentId) {
        super( ErrorCode.COMMENT_NOT_FOUND.name ( ) ,
                "Comment not found with ID: " + commentId);
    }


}