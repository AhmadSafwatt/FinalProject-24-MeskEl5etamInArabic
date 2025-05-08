package com.example.chatservice.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        String errorMessage = ex.getReason() != null ? ex.getReason() : "A ResponseStatusException occurred";
        return ResponseEntity.status(ex.getStatusCode()).body(errorMessage);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException) {
            if (invalidFormatException.getTargetType().isEnum()) {
                String errorMessage = "Invalid value for enum type: " + invalidFormatException.getValue() +
                        ". Accepted values are: " + Arrays.toString(invalidFormatException.getTargetType().getEnumConstants());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed JSON request: " + ex.getMessage());
    }
}