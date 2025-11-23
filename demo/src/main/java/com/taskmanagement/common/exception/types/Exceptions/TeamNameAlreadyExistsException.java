package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.DuplicateResourceException;



public class TeamNameAlreadyExistsException extends DuplicateResourceException {

    public TeamNameAlreadyExistsException(String teamName) {
        super(
                ErrorCode.TEAM_NAME_ALREADY_EXISTS.name(),
                "Team with name '" + teamName + "' already exists"
        );
    }
}