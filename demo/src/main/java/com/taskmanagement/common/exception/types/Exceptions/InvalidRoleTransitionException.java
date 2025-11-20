package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StatuesException;
import com.taskmanagement.user.enums.Role;

public class InvalidRoleTransitionException extends StatuesException {

    public InvalidRoleTransitionException(Role currentRole, Role newRole) {
        super(ErrorCode.INVALID_ROLE_TRANSITION.name(),
                String.format("Cannot change role from %s to %s", currentRole, newRole));
    }

    public InvalidRoleTransitionException(String message) {
        super(ErrorCode.INVALID_ROLE_TRANSITION.name(), message);
    }
}

