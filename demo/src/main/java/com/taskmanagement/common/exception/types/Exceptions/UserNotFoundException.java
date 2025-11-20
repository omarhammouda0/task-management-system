package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.NotFoundException;


public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(Long id) {
        super(
                ErrorCode.USER_NOT_FOUND.name(),
                "User with ID '" + id + "' not found"
        );

    }

    public UserNotFoundException (String email) {
        super(
                ErrorCode.USER_NOT_FOUND.name(),
                "User with email '" + email + "' not found"
        );
    }
}
