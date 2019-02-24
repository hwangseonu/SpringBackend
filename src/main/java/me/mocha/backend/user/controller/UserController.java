package me.mocha.backend.user.controller;

import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.UserRepository;
import me.mocha.backend.common.security.jwt.JwtProvider;
import me.mocha.backend.common.security.jwt.JwtType;
import me.mocha.backend.user.request.SignUpRequest;
import me.mocha.backend.user.response.SignInResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        if (userRepository.existsByUsernameOrNicknameOrEmail(request.getUsername(), request.getNickname(), request.getEmail()))
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        User user = userRepository.save(User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .roles(Collections.singletonList("ROLE_USERS"))
                .build());
        return ResponseEntity.ok().body(new SignInResponse(
                jwtProvider.createToken(user.getUsername(), JwtType.ACCESS),
                jwtProvider.createToken(user.getUsername(), JwtType.REFRESH)));
    }

}
