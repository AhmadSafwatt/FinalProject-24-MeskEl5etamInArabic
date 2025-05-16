package com.example.chatservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class TokenPrinter implements CommandLineRunner {
    @Override
    public void run(String... args) {
        String username = "testuser";
        String token = JwtUtil.generateToken(username, new HashMap<>());
        System.out.println("Generated JWT at startup for '" + username + "':\n" + token);

        String usernameFromToken = JwtUtil.extractUsername(token);
        System.out.println("Username extracted from token: " + usernameFromToken);
        boolean isValid = JwtUtil.validateToken(token);
        if (isValid) {
            System.out.println("Token is valid.");
        } else {
            System.out.println("Token is invalid.");
        }
    }
}