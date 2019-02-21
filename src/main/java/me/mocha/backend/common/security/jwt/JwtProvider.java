package me.mocha.backend.common.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.access.exp}")
    private long accessExp;

    @Value("${jwt.refresh.exp}")
    private long refreshExp;

    @Getter
    private String secret = System.getenv("JWT_SECRET");

    @Autowired
    public JwtProvider() {
        if (!StringUtils.hasText(secret)) secret = "jwt_secret";
    }

    public String createToken(String uuid, JwtType type) {
        return Jwts.builder()
                .setSubject(type.toString())
                .setExpiration(new Date(System.currentTimeMillis() + (type == JwtType.ACCESS ? accessExp : refreshExp)))
                .setIssuedAt(new Date())
                .setNotBefore(new Date())
                .setId(uuid)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getId(String jwt, JwtType type) {
        return Jwts.parser()
                .setSigningKey(secret)
                .requireSubject(type.toString())
                .parseClaimsJwt(jwt).getBody().getId();
    }

    public boolean validToken(String token, JwtType type) {
        try {
            Jwts.parser().requireSubject(type.toString()).setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

}
