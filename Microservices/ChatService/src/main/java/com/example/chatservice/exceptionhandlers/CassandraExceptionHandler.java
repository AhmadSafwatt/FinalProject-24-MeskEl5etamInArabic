package com.example.chatservice.exceptionhandlers;

import com.datastax.oss.driver.api.core.servererrors.ProtocolError;
import com.example.chatservice.utils.ExceptionLoggingUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.cassandra.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@Order(1)
@ControllerAdvice
public class CassandraExceptionHandler {

    @ExceptionHandler(CassandraConnectionFailureException.class)
    public ResponseEntity<String> handleCassandraConnectionFailureException(CassandraConnectionFailureException ex, HttpServletRequest request) {
        String errorMessage = "Failed to connect to Cassandra: " + ex.getMessage();
        ExceptionLoggingUtil.logStructuredError("CassandraConnectionFailureException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorMessage);
    }

    @ExceptionHandler(CassandraWriteTimeoutException.class)
    public ResponseEntity<String> handleCassandraWriteTimeoutException(CassandraWriteTimeoutException ex, HttpServletRequest request) {
        String errorMessage = "Cassandra write timeout: " + ex.getMessage();
        ExceptionLoggingUtil.logStructuredError("CassandraWriteTimeoutException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorMessage);
    }

    @ExceptionHandler(CassandraReadTimeoutException.class)
    public ResponseEntity<String> handleCassandraReadTimeoutException(CassandraReadTimeoutException ex, HttpServletRequest request) {
        String errorMessage = "Cassandra read timeout: " + ex.getMessage();
        ExceptionLoggingUtil.logStructuredError("CassandraReadTimeoutException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorMessage);
    }

    @ExceptionHandler(CassandraInvalidQueryException.class)
    public ResponseEntity<String> handleCassandraInvalidQueryException(CassandraInvalidQueryException ex, HttpServletRequest request) {
        String errorMessage = "Invalid Cassandra query: " + ex.getMessage();
        ExceptionLoggingUtil.logStructuredError("CassandraInvalidQueryException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }


    @ExceptionHandler(CassandraUncategorizedException.class)
    public ResponseEntity<String> handleCassandraUncategorizedException(CassandraUncategorizedException ex, HttpServletRequest request) {
        Throwable cause = ex.getCause();
        if (cause instanceof ProtocolError) {
            String errorMessage = "ProtocolError: " + cause.getMessage();
            ExceptionLoggingUtil.logStructuredError("ProtocolError", errorMessage, request, ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
        String errorMessage = "A Cassandra error occurred: " + ex.getMessage();
        ExceptionLoggingUtil.logStructuredError("CassandraUncategorizedException", errorMessage, request, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }
}