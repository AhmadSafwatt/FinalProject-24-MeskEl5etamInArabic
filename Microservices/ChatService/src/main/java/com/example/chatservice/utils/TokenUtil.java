package com.example.chatservice.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TokenUtil {

    private final JwtUtil jwtUtil;

    @Autowired
    public TokenUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String generateTestToken(String username) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("id", UUID.randomUUID().toString());
        claims.put("username", username);
        claims.put("email", "testuser@gmail.com");
        claims.put("address", "123 Test St, Test City, TC 12345");
        claims.put("phoneNumber", "123-456-7890");


        return jwtUtil.createToken(claims, username);
    }
}
