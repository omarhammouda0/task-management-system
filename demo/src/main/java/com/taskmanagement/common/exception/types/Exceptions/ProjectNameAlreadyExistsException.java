package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.DuplicateResourceException;

public class ProjectNameAlreadyExistsException extends DuplicateResourceException {

    public ProjectNameAlreadyExistsException(String projectName , Long teamId) {

        super (
                ErrorCode.PROJECT_NAME_ALREADY_EXISTS.name ( ) ,
                "Project with name '" + projectName + "' already exists in team with id '" + teamId + "'"
        );
    }
}
