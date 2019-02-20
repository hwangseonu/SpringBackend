package me.mocha.backend.common.model.repository;

import me.mocha.backend.common.model.entity.Token;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.security.jwt.JwtType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    void deleteByOwnerAndUserAgentAndType(User owner, String userAgent, JwtType type);

}
