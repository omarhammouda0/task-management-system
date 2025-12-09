package com.taskmanagement.common.exception.types.Exceptions;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(Long commentId) {
        super("Comment not found with ID: " + commentId);
    }

    public CommentNotFoundException(String message) {
        super(message);
    }
}