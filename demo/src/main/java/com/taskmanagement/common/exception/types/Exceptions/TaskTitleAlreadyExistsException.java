package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.DuplicateResourceException;


public class TaskTitleAlreadyExistsException extends DuplicateResourceException {

    public TaskTitleAlreadyExistsException(String tile , Long projectId) {
        super(
                ErrorCode.TASK_TITLE_ALREADY_EXISTS.name (),
                "Task with title '" + tile + "' already exists in project with id: " + projectId
        );

    }
}
