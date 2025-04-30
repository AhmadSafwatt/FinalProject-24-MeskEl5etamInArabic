package com.homechef.API_Gateway.utils;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;


@Component
public class JwtUtil {

    // this class is used to create a JWT token
    // and validate it
    // it is used to authenticate the user before allowing access to the API Gateway
    // and subsequently to the microservices our application offers

    //TODO: get from the application.yml file
    private static final String SECRET = "${jwt.secret}";



    public void validateToken(final String token) {
        Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }


    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
