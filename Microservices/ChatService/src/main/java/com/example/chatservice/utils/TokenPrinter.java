package com.example.chatservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class TokenPrinter implements CommandLineRunner {

    private final JwtUtil jwUtil;
    private final TokenUtil tokenUtil;

    @Autowired
    public TokenPrinter(JwtUtil jwtUtil, TokenUtil tokenUtil) {
        this.jwUtil = jwtUtil;
        this.tokenUtil = tokenUtil;
    }

    @Override
    public void run(String... args) {

        UUID userId = UUID.randomUUID();

        String token = tokenUtil.generateTestToken(userId);
        System.out.println("Generated JWT at startup for " + userId + ":\n" + token);

        String userIdFromToken = jwUtil.extractUserId(token);
        System.out.println("id extracted from token: " + userIdFromToken);
        boolean isValid = jwUtil.validateToken(token);
        if (isValid) {
            System.out.println("Token is valid.");
        } else {
            System.out.println("Token is invalid.");
        }
    }
}