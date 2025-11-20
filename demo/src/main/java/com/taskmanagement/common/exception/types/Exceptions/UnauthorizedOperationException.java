package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StatuesException;

public class UnauthorizedOperationException extends StatuesException {

    public UnauthorizedOperationException(String operation, Long userId) {
        super(ErrorCode.UNAUTHORIZED_OPERATION.name(),
                String.format("Unauthorized to perform operation '%s' on user with id %d", operation, userId));
    }

    public UnauthorizedOperationException(String message) {
        super(ErrorCode.UNAUTHORIZED_OPERATION.name(), message);
    }
}

