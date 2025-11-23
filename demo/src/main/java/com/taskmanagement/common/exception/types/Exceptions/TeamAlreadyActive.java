package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.DuplicateResourceException;


public class TeamAlreadyActive extends DuplicateResourceException {

    public TeamAlreadyActive(Long teamID) {
        super(
                ErrorCode.TEAM_ALREADY_ACTIVE.name(),
                "The team with ID '" + teamID + "' is already activated'"
        );
    }
}