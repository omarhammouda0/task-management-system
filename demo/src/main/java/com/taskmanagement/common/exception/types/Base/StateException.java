package com.taskmanagement.common.exception.types.Base;

import com.taskmanagement.common.exception.base.AppException;
import org.springframework.http.HttpStatus;


public class StateException extends AppException {
    public StateException(String code, String message) {
        super( HttpStatus.BAD_REQUEST, code, message);
    }
}
