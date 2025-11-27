package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {

    public ProjectNotFoundException(Long id) {
        super (
                ErrorCode.PROJECT_NOT_FOUND.name ( ) ,
                "Project with ID '" + id + "' not found"
        );

    }

    public  ProjectNotFoundException (String name) {
        super (
                ErrorCode.PROJECT_NOT_FOUND.name ( ) ,
                "Project with name '" + name + "' not found"
        );
    }
}