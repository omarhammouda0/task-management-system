package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StatuesException;

public class SelfOperationNotAllowedException extends StatuesException {

    public SelfOperationNotAllowedException(String operation) {
        super(ErrorCode.SELF_OPERATION_NOT_ALLOWED.name(),
                String.format("Cannot perform '%s' operation on your own account", operation));
    }
}

