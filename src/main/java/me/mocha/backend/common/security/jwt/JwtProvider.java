package me.mocha.backend.common.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import me.mocha.backend.common.model.entity.Token;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    @Value("${jwt.access.exp}")
    private long accessExp;

    @Value("${jwt.refresh.exp}")
    private long refreshExp;

    private String secret = System.getenv("JWT_SECRET");

    private final TokenRepository tokenRepository;

    @Autowired
    public JwtProvider(TokenRepository tokenRepository) {
        if (!StringUtils.hasText(secret)) secret = "jwt_secret";
        this.tokenRepository = tokenRepository;
    }

    public String createToken(User user, String userAgent, JwtType type) {
        UUID id = UUID.randomUUID();

        tokenRepository.deleteByOwnerAndUserAgentAndType(user, userAgent, type);
        tokenRepository.save(new Token(
           id,
           user,
           userAgent,
           type
        ));

        return Jwts.builder()
                .setSubject(type.toString())
                .setExpiration(new Date(System.currentTimeMillis() + (type == JwtType.ACCESS ? accessExp : refreshExp)))
                .setIssuedAt(new Date())
                .setNotBefore(new Date())
                .setId(id.toString())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

}
