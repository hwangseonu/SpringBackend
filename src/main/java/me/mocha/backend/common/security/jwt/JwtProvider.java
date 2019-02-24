package me.mocha.backend.common.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    @Value("${jwt.access.exp}")
    private long accessExp;

    @Value("${jwt.refresh.exp}")
    private long refreshExp;

    private String secret = System.getenv("JWT_SECRET");

    public JwtProvider() {
        if (secret == null) secret = "jwt-secret";
    }

    public String createToken(String username, JwtType type) {
        return Jwts.builder()
                .setSubject(type.toString())
                .setId(UUID.randomUUID().toString())
                .setExpiration(new Date(System.currentTimeMillis() + (type == JwtType.ACCESS ? accessExp : refreshExp)))
                .setNotBefore(new Date())
                .setIssuedAt(new Date())
                .claim("identity", username)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getUsername(String jwt) {
        return Jwts.parser().parseClaimsJws(jwt).getBody().get("identity", String.class);
    }

    public LocalDateTime getExpiration(String jwt) {
        return LocalDateTime.ofInstant(Jwts.parser().parseClaimsJws(jwt).getBody().getExpiration().toInstant(), ZoneId.systemDefault());
    }

    public boolean isValid(String jwt, JwtType type) {
        try {
            Jwts.parser().requireSubject(type.toString()).parseClaimsJws(jwt);
            return true;
        } catch (JwtException e) {
            return true;
        }
    }

}
