package com.taskmanagement.common.exception.handler;

import com.taskmanagement.common.exception.base.AppException;
import com.taskmanagement.common.exception.types.Base.DuplicateResourceException;
import com.taskmanagement.common.exception.types.Base.NotFoundException;
import com.taskmanagement.common.exception.types.Base.StatuesException;
import com.taskmanagement.common.exception.types.Exceptions.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleInvalidInput(HttpMessageNotReadableException ex , WebRequest request) {

        String message = ex.getMessage ( );
        String errorDetail;
        String errorCode;

        if (message != null) {


            if (message.contains ( "not one of the values accepted for Enum class" )) {
                errorCode = "INVALID_ENUM_VALUE";
                errorDetail = "Invalid value provided for an enum field. Please check the allowed values for your input.";
            } else if (message.contains ( "Cannot deserialize value of type" ) &&
                    (message.contains ( "Instant" ) || message.contains ( "LocalDate" ) || message.contains ( "LocalDateTime" ))) {
                errorCode = "INVALID_DATE_FORMAT";
                errorDetail = "Invalid date/time format. Please use ISO-8601 format (e.g., 2025-11-26T00:16:25.430Z).";
            } else if (message.contains ( "not a valid" ) && (message.contains ( "Integer" ) || message.contains ( "Long" ))) {
                errorCode = "INVALID_NUMBER_FORMAT";
                errorDetail = "Invalid number format. Please provide a valid numeric value.";
            } else if (message.contains ( "required" ) || message.contains ( "missing" )) {
                errorCode = "MISSING_REQUIRED_FIELD";
                errorDetail = "Required field is missing. Please check your request body.";
            } else if (message.contains ( "Unexpected character" ) || message.contains ( "JSON parse error" )) {
                errorCode = "INVALID_JSON_SYNTAX";
                errorDetail = "Invalid JSON format. Please check your request syntax.";
            } else {
                errorCode = "INVALID_REQUEST_FORMAT";
                errorDetail = "Invalid request format. Please check your input data.";
            }
        } else {
            errorCode = "INVALID_REQUEST_FORMAT";
            errorDetail = "Invalid request format. Please check your input data.";
        }

        log.error ( "Invalid input: {}" , errorDetail );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail (
                HttpStatus.BAD_REQUEST ,
                errorDetail
        );
        problemDetail.setTitle ( "Invalid Request" );
        problemDetail.setProperty ( "code" , errorCode );
        problemDetail.setProperty ( "timestamp" , Instant.now ( ) );
        problemDetail.setProperty ( "path" , request.getDescription ( false ).replace ( "uri=" , "" ) );

        return problemDetail;
    }


    @ExceptionHandler(AppException.class)
    public ProblemDetail handleAppException(AppException ex, WebRequest request) {
        log.error("Application exception: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                ex.getStatus(),
                ex.getMessage()
        );
        problemDetail.setTitle(ex.getClass().getSimpleName());
        problemDetail.setProperty("code", ex.getCode());
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields"
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("code", "VALIDATION_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        log.error("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Constraint violation occurred"
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("code", "CONSTRAINT_VIOLATION");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }


    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail handleSpringSecurityAccessDenied(org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        log.error("Spring Security access denied: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "You don't have permission to access this resource"
        );
        problemDetail.setTitle("Access Denied");
        problemDetail.setProperty("code", "ACCESS_DENIED");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }



    @ExceptionHandler({BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleInvalidCredentials(Exception ex, WebRequest request) {
        log.error("Invalid credentials: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
        );
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setProperty("code", "INVALID_CREDENTIALS");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }




    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.error("Authentication failed: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed: " + ex.getMessage()
        );
        problemDetail.setTitle("Authentication Error");
        problemDetail.setProperty("code", "AUTHENTICATION_FAILED");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid Request");
        problemDetail.setProperty("code", "INVALID_ARGUMENT");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        log.warn("Illegal state: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid Operation");
        problemDetail.setProperty("code", "INVALID_STATE");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }

    @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleMissingServletRequestPart(
            org.springframework.web.multipart.support.MissingServletRequestPartException ex,
            WebRequest request) {

        log.warn("Missing request part: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "File is required. Please select a file to upload."
        );
        problemDetail.setTitle("Missing File");
        problemDetail.setProperty("code", "MISSING_FILE");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }


    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleMaxUploadSizeExceeded(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex,
            WebRequest request) {

        log.warn("File size exceeded: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "File size exceeds the maximum allowed size of 10 MB"
        );
        problemDetail.setTitle("File Too Large");
        problemDetail.setProperty("code", "FILE_TOO_LARGE");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleMissingServletRequestParameter(
            org.springframework.web.bind.MissingServletRequestParameterException ex,
            WebRequest request) {

        log.warn("Missing request parameter: {}", ex.getMessage());

        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                message
        );
        problemDetail.setTitle("Missing Parameter");
        problemDetail.setProperty("code", "MISSING_PARAMETER");
        problemDetail.setProperty("parameterName", ex.getParameterName());
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleMethodArgumentTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex,
            WebRequest request) {

        log.warn("Type mismatch: {}", ex.getMessage());

        String message = String.format(
                "Parameter '%s' should be of type '%s' but received '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                ex.getValue()
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                message
        );
        problemDetail.setTitle("Type Mismatch");
        problemDetail.setProperty("code", "TYPE_MISMATCH");
        problemDetail.setProperty("parameterName", ex.getName());
        problemDetail.setProperty("expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex,
            WebRequest request) {

        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity constraint violated";
        String errorCode = "DATA_INTEGRITY_VIOLATION";

        if (ex.getMessage() != null) {
            String exMessage = ex.getMessage().toLowerCase();

            if (exMessage.contains("duplicate key") || exMessage.contains("unique constraint")) {
                message = "A record with this value already exists";
                errorCode = "DUPLICATE_ENTRY";
            } else if (exMessage.contains("foreign key")) {
                message = "Cannot perform operation due to existing relationships";
                errorCode = "FOREIGN_KEY_VIOLATION";
            } else if (exMessage.contains("not-null") || exMessage.contains("null")) {
                message = "Required field cannot be null";
                errorCode = "NULL_VALUE_NOT_ALLOWED";
            }
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                message
        );
        problemDetail.setTitle("Data Integrity Violation");
        problemDetail.setProperty("code", errorCode);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNoHandlerFound(
            org.springframework.web.servlet.NoHandlerFoundException ex,
            WebRequest request) {

        log.warn("Endpoint not found: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        String message = String.format(
                "No endpoint found for %s %s",
                ex.getHttpMethod(),
                ex.getRequestURL()
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                message
        );
        problemDetail.setTitle("Endpoint Not Found");
        problemDetail.setProperty("code", "ENDPOINT_NOT_FOUND");
        problemDetail.setProperty("method", ex.getHttpMethod());
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleGenericException(Exception ex, WebRequest request) {

        log.error("Unexpected error: ", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support if the problem persists."
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("code", "INTERNAL_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }
}

