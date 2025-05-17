package com.example.chatservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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

        String username = "testUser";

        String token = tokenUtil.generateTestToken(username);
        System.out.println("Generated JWT at startup for " + username + ":\n" + token);

        String usernameFromToken = jwUtil.extractUsername(token);
        System.out.println("username extracted from token: " + usernameFromToken);
        Jws<Claims> validateToken = jwUtil.validateToken(token);
        if (validateToken != null) {
            System.out.println("Token is valid.");
        } else {
            System.out.println("Token is invalid.");
        }
    }
}