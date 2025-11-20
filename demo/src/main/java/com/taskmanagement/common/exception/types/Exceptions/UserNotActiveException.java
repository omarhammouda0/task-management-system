package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StatuesException;




public class UserNotActiveException extends StatuesException {

    public UserNotActiveException(String email) {
        super(
                ErrorCode.USER_NOT_ACTIVE.name(),
                "User with email '" + email + "' is not active"
        );
    }
}