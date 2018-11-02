package me.mocha.backend.controller;

import me.mocha.backend.auth.Jwt.JwtTokenProvider;
import me.mocha.backend.auth.Jwt.JwtType;
import me.mocha.backend.exception.account.PasswordIncorrectException;
import me.mocha.backend.exception.account.TokenRefreshException;
import me.mocha.backend.exception.account.UserNotFoundException;
import me.mocha.backend.model.entity.User;
import me.mocha.backend.model.repository.UserRepository;
import me.mocha.backend.payload.auth.SignInRequest;
import me.mocha.backend.payload.auth.SignInResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    public AuthController(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping
    public SignInResponse signIn(@Valid @RequestBody SignInRequest request) throws UserNotFoundException, PasswordIncorrectException {
        User found = userRepository.findById(request.getUsername()).orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(request.getPassword(), found.getPassword())) throw new PasswordIncorrectException();
        return new SignInResponse(tokenProvider.generateToken(found.getUsername(), JwtType.ACCESS), tokenProvider.generateToken(found.getUsername(), JwtType.REFRESH));
    }

    @GetMapping("/refresh")
    public SignInResponse refresh(@RequestHeader("Authorization") String header) throws TokenRefreshException {
        if (header.startsWith("Bearer ")) {
            String token = header.replaceFirst("Bearer ", "");
            if (StringUtils.hasText(token) && tokenProvider.validToken(token, JwtType.REFRESH)) {
                String username = tokenProvider.getUsernameFromToken(token, JwtType.REFRESH);
                if (userRepository.existsById(username)) {
                    SignInResponse response = new SignInResponse(tokenProvider.generateToken(username, JwtType.ACCESS));
                    if (ChronoUnit.DAYS.between(LocalDate.now(), tokenProvider.getExpiration(token)) <= 7) {
                        response.setRefreshToken(tokenProvider.generateToken(username, JwtType.REFRESH));
                    }
                    return response;
                }
            }
        }
        throw new TokenRefreshException();
    }

}
