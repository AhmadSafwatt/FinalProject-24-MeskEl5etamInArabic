package com.example.chatservice.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
public class ExceptionLoggingUtil {

    public static void logStructuredError(String exceptionType, String message, HttpServletRequest request, Exception ex) {
        String readableTimestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(System.currentTimeMillis()));

        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String queryParams = request.getQueryString();

        String userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            userId = (String) authentication.getPrincipal();
        }

        String logDetails = "Exception occurred:\n" +
                "Timestamp: " + readableTimestamp + "\n" +
                "User ID: " + userId + "\n" +
                "Exception Type: " + exceptionType + "\n" +
                "Message: " + message + "\n" +
                "Endpoint: " + request.getRequestURI() + "\n" +
                "HTTP Method: " + request.getMethod() + "\n" +
                "\n" +
                "Client IP: " + clientIp + "\n" +
                "User-Agent: " + userAgent + "\n" +
                "Query Parameters: " + queryParams + "\n";

        log.error(logDetails, ex);
    }
}