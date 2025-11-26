package com.taskmanagement.common.exception.types.Exceptions;

import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.StatuesException;

public class InvalidProjectStatusException extends StatuesException {

    public InvalidProjectStatusException(String code , String message) {
        super (

                ErrorCode.INVALID_PROJECT_STATUS.name ( ) ,
                "Invalid status for the project : " + message

        );


    }
}
