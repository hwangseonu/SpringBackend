package me.mocha.backend.user.controller;

import me.mocha.backend.common.model.entity.Token;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.TokenRepository;
import me.mocha.backend.common.model.repository.UserRepository;
import me.mocha.backend.common.security.jwt.JwtProvider;
import me.mocha.backend.common.security.jwt.JwtType;
import me.mocha.backend.user.request.SignUpRequest;
import me.mocha.backend.user.response.SignInResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    @Autowired
    public UserController(PasswordEncoder passwordEncoder, TokenRepository tokenRepository, UserRepository userRepository, JwtProvider jwtProvider) {
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }


    @PostMapping
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request, @RequestHeader("user-agent") String userAgent) {
        if (userRepository.existsByUsernameOrNicknameOrEmail(request.getUsername(), request.getNickname(), request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = userRepository.save(User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .roles(Collections.singletonList("ROLE_USER"))
                .build());

        Token access = tokenRepository.save(new Token(UUID.randomUUID(), user, userAgent, JwtType.ACCESS));
        Token refresh = tokenRepository.save(new Token(UUID.randomUUID(), user, userAgent, JwtType.REFRESH));

        return ResponseEntity.ok(new SignInResponse(
                jwtProvider.createToken(access.getIdentity().toString(), JwtType.ACCESS),
                jwtProvider.createToken(refresh.getIdentity().toString(), JwtType.REFRESH)));
    }

}
