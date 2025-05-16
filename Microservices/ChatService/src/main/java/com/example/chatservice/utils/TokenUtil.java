package com.example.chatservice.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenUtil {

    public static String generateTestToken(UUID userId) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("id", userId.toString());
        claims.put("username", "testuser");
        claims.put("email", "testuser@gmail.com");
        claims.put("address", "123 Test St, Test City, TC 12345");
        claims.put("phoneNumber", "123-456-7890");


        return JwtUtil.generateToken(String.valueOf(userId), claims);
    }
}
