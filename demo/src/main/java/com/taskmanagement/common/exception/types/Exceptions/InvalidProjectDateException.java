package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StateException;


import java.time.LocalDate;

public class InvalidProjectDateException extends StateException {

    public InvalidProjectDateException(String message) {

        super (
                ErrorCode.INVALID_PROJECT_DATE.name ( ) ,
                message
        );
    }
}
