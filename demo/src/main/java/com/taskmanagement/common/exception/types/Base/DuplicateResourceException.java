package com.taskmanagement.common.exception.types.Base;

import com.taskmanagement.common.exception.base.AppException;
import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends AppException {
    public DuplicateResourceException(String code, String message) {
        super( HttpStatus.CONFLICT, code, message);
    }
}

