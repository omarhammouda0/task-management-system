package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.DuplicateResourceException;


public class TeamAlreadyArchivedException extends DuplicateResourceException {

    public TeamAlreadyArchivedException(Long teamID) {
        super(
                ErrorCode.TEAM_ALREADY_ARCHIVED.name(),
                "The team with ID '" + teamID + "' already archived'"
        );
    }
}