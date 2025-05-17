package com.example.chatservice.exceptionhandlers;

import com.example.chatservice.utils.ExceptionLoggingUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@Order(0)
@ControllerAdvice
public class AuthenticationExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String errorMessage = "Authentication failed: " + ex.getMessage();
        ExceptionLoggingUtil.logStructuredError("AuthenticationException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
    }
}