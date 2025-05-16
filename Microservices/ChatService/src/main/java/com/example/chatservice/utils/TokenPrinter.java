package com.example.chatservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class TokenPrinter implements CommandLineRunner {
    @Override
    public void run(String... args) {

        UUID userId = UUID.randomUUID();

        String token = TokenUtil.generateTestToken(userId);
        System.out.println("Generated JWT at startup for " + userId + ":\n" + token);

        String userIdFromToken = JwtUtil.extractUserId(token);
        System.out.println("id extracted from token: " + userIdFromToken);
        boolean isValid = JwtUtil.validateToken(token);
        if (isValid) {
            System.out.println("Token is valid.");
        } else {
            System.out.println("Token is invalid.");
        }
    }
}