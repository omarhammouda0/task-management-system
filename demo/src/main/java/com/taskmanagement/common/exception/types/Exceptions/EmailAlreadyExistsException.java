package com.taskmanagement.common.exception.types.Exceptions;


import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.DuplicateResourceException;

public class EmailAlreadyExistsException extends DuplicateResourceException {

    public EmailAlreadyExistsException(String email) {
        super(
                ErrorCode.EMAIL_ALREADY_REGISTERED.name(),
                "User with email '" + email + "' already exists"
        );
    }
}
