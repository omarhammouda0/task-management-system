package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StatuesException;


public class LastAdminException extends StatuesException {

    public LastAdminException(Long userId) {
        super(
                ErrorCode.CANNOT_DELETE_LAST_ADMIN.name(),
                "Cannot delete the last admin user with id '" + userId + "'"
        );
    }
}
