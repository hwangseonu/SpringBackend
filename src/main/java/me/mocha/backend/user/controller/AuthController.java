package me.mocha.backend.user.controller;

import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.UserRepository;
import me.mocha.backend.common.security.jwt.JwtProvider;
import me.mocha.backend.common.security.jwt.JwtType;
import me.mocha.backend.user.request.SignInRequest;
import me.mocha.backend.user.response.SignInResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping
    public ResponseEntity<SignInResponse> signIn(@Valid @RequestBody SignInRequest request) {
        User user = userRepository.findById(request.getUsername()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(new SignInResponse(
                jwtProvider.createToken(user.getUsername(), JwtType.ACCESS),
                jwtProvider.createToken(user.getUsername(), JwtType.REFRESH)));
    }

    @GetMapping("/refresh")
    public ResponseEntity<SignInResponse> refresh(@RequestHeader("Authorization") String header) {
        if (StringUtils.hasText(header) && header.startsWith("Bearer")) {
            String jwt = header.replaceFirst("Bearer", "").trim();
            if (StringUtils.hasText(jwt) && jwtProvider.isValid(jwt, JwtType.REFRESH)) {
                String username = jwtProvider.getUsername(jwt);
                if (userRepository.existsById(username)) {
                    SignInResponse response = new SignInResponse(jwtProvider.createToken(username, JwtType.ACCESS), null);
                    if (ChronoUnit.DAYS.between(LocalDate.now(), jwtProvider.getExpiration(jwt)) <= 7) {
                        response.setRefresh(jwtProvider.createToken(username, JwtType.REFRESH));
                    }
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } else {
                return ResponseEntity.unprocessableEntity().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
