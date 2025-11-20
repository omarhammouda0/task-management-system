package com.taskmanagement.common.exception.handler;


import com.taskmanagement.common.exception.base.AppException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice


public class ApiExceptionHandler {


    @ExceptionHandler(AppException.class)
    @ResponseStatus
    public ProblemDetail handleApp(AppException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatus());
        pd.setTitle(ex.getClass().getSimpleName());
        pd.setDetail(ex.getMessage());
        pd.setProperty("code", ex.getCode());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus

    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String,String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect( Collectors.toMap( FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage(),
                        (a,b) -> a));
        ProblemDetail pd = ProblemDetail.forStatus( HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setDetail("One or more fields are invalid.");
        pd.setProperty("errors", errors);
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }






}