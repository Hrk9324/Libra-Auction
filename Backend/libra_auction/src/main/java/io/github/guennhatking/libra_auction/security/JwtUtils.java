package io.github.guennhatking.libra_auction.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;


@Component
public class JwtUtils {
    // This class will contain utility methods for generating and validating JWT tokens.
    // You can use the jjwt library to implement these methods.

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    private Key getSigningKey() {
        // Implement this method to return the signing key based on your secret
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateJwtToken(String nguoiDungId, String email) {
        // Implement this method to generate a JWT token for the given username
        return Jwts.builder()
                .setSubject(nguoiDungId)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
}