package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.DuplicateResourceException;


public class TeamAlreadyDeletedException extends DuplicateResourceException {

    public TeamAlreadyDeletedException(Long teamID) {
        super(
                ErrorCode.TEAM_ALREADY_DELETED.name(),
                "The team with ID '" + teamID + "' already deleted'"
        );
    }
}