package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.NotFoundException;


public class TaskNotFoundException extends NotFoundException {

    public TaskNotFoundException(Long taskId) {
        super (
                ErrorCode.TASK_NOT_FOUND.name ( ) ,
                "Task with ID '" + taskId + "' not found"

        );

    }

}