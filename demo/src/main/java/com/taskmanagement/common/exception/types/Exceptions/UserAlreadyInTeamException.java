package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.DuplicateResourceException;


public class UserAlreadyInTeamException extends DuplicateResourceException {

    public UserAlreadyInTeamException(Long userID , Long teamID) {
        super(
                ErrorCode.USER_ALREADY_IN_TEAM.name(),
                "The user with ID '" + userID + "' already exists in the team with ID '" + teamID + "'"
        );
    }
}