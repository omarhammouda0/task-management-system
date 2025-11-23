package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.DuplicateResourceException;
import com.taskmanagement.common.exception.types.Base.NotFoundException;


public class UserNotInTeamException extends NotFoundException {

    public UserNotInTeamException(Long userID , Long teamID) {
        super(
                ErrorCode.USER_NOT_FOUND_IN_TEAM.name(),
                "The user with ID '" + userID + "' not exists in the team with ID '" + teamID + "'"
        );
    }
}