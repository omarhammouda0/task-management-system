package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StatuesException;

public class InvalidPasswordException extends StatuesException {

    public InvalidPasswordException(String message) {
        super(ErrorCode.INVALID_PASSWORD.name(), message);
    }

    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD.name(),
                "The provided password is incorrect");
    }
}

