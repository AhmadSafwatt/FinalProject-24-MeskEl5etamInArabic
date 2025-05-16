package com.example.chatservice.exceptionhandlers;

import com.example.chatservice.utils.ExceptionLoggingUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Order(2)
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        ExceptionLoggingUtil.logStructuredError("ResponseStatusException", ex.getReason(), request, ex);
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<String> handleInvalidFormatException(InvalidFormatException ex, HttpServletRequest request) {
        String errorMessage = "Invalid format for value: " + ex.getValue() + ". Expected type: " + ex.getTargetType().getSimpleName();
        ExceptionLoggingUtil.logStructuredError("InvalidFormatException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<String> handleJsonParseException(JsonParseException ex, HttpServletRequest request) {
        String errorMessage = "JSON parsing error: " + ex.getOriginalMessage();
        ExceptionLoggingUtil.logStructuredError("JsonParseException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        String errorMessage = "Access denied: " + ex.getMessage();
        ExceptionLoggingUtil.logStructuredError("AccessDeniedException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        String errorMessage = "Illegal argument: " + ex.getMessage();
        ExceptionLoggingUtil.logStructuredError("IllegalArgumentException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(NullPointerException ex, HttpServletRequest request) {
        String errorMessage = "Null pointer exception: " + ex.getMessage();
        ExceptionLoggingUtil.logStructuredError("NullPointerException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex, HttpServletRequest request) {
        String errorMessage = "An unexpected error occurred: " + ex.getMessage();
        ExceptionLoggingUtil.logStructuredError("GenericException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }
}