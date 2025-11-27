package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StatuesException;
import com.taskmanagement.project.enums.ProjectStatus;

public class InvalidProjectStatusException extends StatuesException {

    public InvalidProjectStatusException(ProjectStatus status) {

        super (
                ErrorCode.INVALID_PROJECT_STATUS.name ( ) ,
                "The project status '" + status + "' is invalid."
        );

    }

    public InvalidProjectStatusException (ProjectStatus oldStatus, ProjectStatus newStatus) {

        super (
                ErrorCode.INVALID_PROJECT_STATUS.name ( ) ,
                "Cannot change project status from '" + oldStatus + "' to '" + newStatus + "'."
        );
    }

}
