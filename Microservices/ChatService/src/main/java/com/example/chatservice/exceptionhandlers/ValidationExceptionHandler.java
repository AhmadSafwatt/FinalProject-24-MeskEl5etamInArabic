package com.example.chatservice.exceptionhandlers;

import com.example.chatservice.utils.ExceptionLoggingUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest httpServletRequest) {
        StringBuilder errorMessage = new StringBuilder("Validation failed for the following fields:\n");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("\n");
        }

        ExceptionLoggingUtil.logStructuredError("MethodArgumentNotValidException", errorMessage.toString(), httpServletRequest, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .headers(new HttpHeaders())
                .body(errorMessage.toString());
    }
}