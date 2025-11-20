package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.AccessException;


public class AccessDeniedException extends AccessException {

    public AccessDeniedException(String message) {
        super(
                ErrorCode.UNAUTHORIZED_OPERATION.name(),
                message
        );
    }
}
