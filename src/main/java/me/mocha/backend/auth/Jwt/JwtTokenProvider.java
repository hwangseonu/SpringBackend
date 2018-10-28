package me.mocha.backend.auth.Jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    public String generateToken(String username, JwtType type) {
        return Jwts.builder()
                .setSubject(type.toString())
                .setExpiration(new Date(System.currentTimeMillis() + (type == JwtType.ACCESS ? accessExpiration : refreshExpiration)))
                .setId(username)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getUsernameFromToken(String token, JwtType type) {
        return Jwts.parser()
                .requireSubject(type.toString())
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody().getId();
    }

    public LocalDateTime getExpiration(String token) {
        return LocalDateTime.ofInstant(Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getExpiration().toInstant(), ZoneId.systemDefault());
    }

    public boolean validToken(String token, JwtType type) {
        try {
            Jwts.parser().requireSubject(type.toString()).setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.error("Invalid Token Error - {}", e.getMessage());
            return false;
        }
    }
}
