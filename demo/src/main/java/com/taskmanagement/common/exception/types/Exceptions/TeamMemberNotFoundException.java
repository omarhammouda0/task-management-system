package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.NotFoundException;


public class TeamMemberNotFoundException extends NotFoundException {

    public TeamMemberNotFoundException(Long userId, Long teamId) {
        super (
                ErrorCode.TEAM_MEMBER_NOT_FOUND.name ( ) ,
                "User with ID '" + userId + "' not found in the Team with ID '" + teamId + "'"
        );

    }

}