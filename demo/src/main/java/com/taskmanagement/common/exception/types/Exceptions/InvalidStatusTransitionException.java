package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StatuesException;


public class InvalidStatusTransitionException extends StatuesException {

    public InvalidStatusTransitionException(String email) {
        super(
                ErrorCode.INVALID_STATUES_TRANSITION.name(),
                "Invalid statues transition , please make a valid request "
        );
    }
}