package com.taskmanagement.common.exception.types.Base;

import com.taskmanagement.common.exception.base.AppException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {
    public NotFoundException(String code, String message) {
        super( HttpStatus.NOT_FOUND, code, message);
    }
}
