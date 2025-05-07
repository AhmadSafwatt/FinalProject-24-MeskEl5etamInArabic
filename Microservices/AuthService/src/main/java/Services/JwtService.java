package Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class JwtService {
    // cannot use values from application.yml because of the requirement to be a pure singleton
    // and not a spring bean which is required for reading from .yml
    public static final String SECRET = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final JwtService instance = new JwtService();

    private JwtService() {}

    public static JwtService getInstance() {
        return instance;
    }

    public Jws<Claims> validateToken(final String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())  // Replaces setSigningKey()
                .build()
                .parseSignedClaims(token); // Replaces parseClaimsJws()
    }


    public String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
