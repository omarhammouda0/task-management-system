package com.taskmanagement.common.exception.types.Base;

import com.taskmanagement.common.exception.base.AppException;
import org.springframework.http.HttpStatus;

public class AccessException extends AppException {
    public AccessException(String code, String message) {
        super( HttpStatus.FORBIDDEN, code, message);
    }
}
