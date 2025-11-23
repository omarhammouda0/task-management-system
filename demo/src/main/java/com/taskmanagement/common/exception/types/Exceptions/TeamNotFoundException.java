package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.NotFoundException;



public class TeamNotFoundException extends NotFoundException {

    public TeamNotFoundException(Long id) {
        super (
                ErrorCode.TEAM_NOT_FOUND.name ( ) ,
                "Team with ID '" + id + "' not found"
        );

    }

    public  TeamNotFoundException (String name) {
        super (
                ErrorCode.TEAM_NOT_FOUND.name ( ) ,
                "Team with name '" + name + "' not found"
        );
    }

}