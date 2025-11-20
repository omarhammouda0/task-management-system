package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StatuesException;



public class UserSuspendedException extends StatuesException {

    public UserSuspendedException(String email) {
        super(
                ErrorCode.USER_IS_SUSPENDED.name(),
                "User with email '" + email + "' is suspended"
        );
    }
}