package me.mocha.backend.controller;

import me.mocha.backend.annotation.CurrentUser;
import me.mocha.backend.auth.Jwt.JwtTokenProvider;
import me.mocha.backend.auth.Jwt.JwtType;
import me.mocha.backend.exception.account.PasswordIncorrectException;
import me.mocha.backend.exception.account.TokenRefreshException;
import me.mocha.backend.exception.account.UserAlreadyExistsException;
import me.mocha.backend.exception.account.UserNotFoundException;
import me.mocha.backend.model.repository.UserRepository;
import me.mocha.backend.model.entity.User;
import me.mocha.backend.payload.account.InfoResponse;
import me.mocha.backend.payload.account.LoginRequest;
import me.mocha.backend.payload.account.LoginResponse;
import me.mocha.backend.payload.account.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    public AccountController(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/verify/{type}")
    public ResponseEntity<?> verify(@PathVariable("type") String type, @RequestBody Map<String, String> map) {
        try {
            if (type.equals("username")) {
                return userRepository.existsByUsername(map.get("username")) ?
                        ResponseEntity.status(HttpStatus.CONFLICT).body(null) : ResponseEntity.ok(null);
            }
            if (type.equals("email")) {
                return userRepository.existsByEmail(map.get("email")) ?
                        ResponseEntity.status(HttpStatus.CONFLICT).body(null) : ResponseEntity.ok(null);
            }
            if (type.equals("nickname")) {
                return userRepository.existsByNickname(map.get("nickname")) ?
                        ResponseEntity.status(HttpStatus.CONFLICT).body(null) : ResponseEntity.ok(null);
            }
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) throws UserAlreadyExistsException {
        if (userRepository.existsByUsernameOrEmailOrNickname(request.getUsername(), request.getEmail(), request.getNickname())) {
            throw new UserAlreadyExistsException();
        }
        userRepository.save(
                User.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .nickname(request.getNickname())
                        .email(request.getEmail())
                .build()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) throws UserNotFoundException, PasswordIncorrectException {
        User found = userRepository.findById(request.getUsername()).orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(request.getPassword(), found.getPassword())) throw new PasswordIncorrectException();
        return new LoginResponse(tokenProvider.generateToken(found.getUsername(), JwtType.ACCESS), tokenProvider.generateToken(found.getUsername(), JwtType.REFRESH));
    }

    @GetMapping("/refresh")
    public LoginResponse refresh(@RequestHeader("Authorization") String header) throws TokenRefreshException{
        if (header.startsWith("Bearer ")) {
            String token = header.replaceFirst("Bearer ", "");
            if (StringUtils.hasText(token) && tokenProvider.validToken(token, JwtType.REFRESH)) {
                String username = tokenProvider.getUsernameFromToken(token, JwtType.REFRESH);
                if (userRepository.existsById(username)) {
                    LoginResponse response = new LoginResponse(tokenProvider.generateToken(username, JwtType.ACCESS));
                    if (Period.between(LocalDate.now(), tokenProvider.getExpiration(token).toLocalDate()).getDays() <= 7) {
                        response.setRefreshToken(tokenProvider.generateToken(username, JwtType.REFRESH));
                    }
                    return response;
                }
            }
        }
        throw new TokenRefreshException();
    }

    @GetMapping("/info")
    public InfoResponse info(@CurrentUser User user) {
        return new InfoResponse(user.getUsername(), user.getEmail(), user.getNickname());
    }

}
