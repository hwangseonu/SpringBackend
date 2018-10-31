package me.mocha.backend.controller;

import me.mocha.backend.annotation.CurrentUser;
import me.mocha.backend.exception.account.UserAlreadyExistsException;
import me.mocha.backend.model.repository.UserRepository;
import me.mocha.backend.model.entity.User;
import me.mocha.backend.payload.user.InfoResponse;
import me.mocha.backend.payload.user.SignUpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserController(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@Valid @RequestBody SignUpRequest request) throws UserAlreadyExistsException {
        if (userRepository.existsByUsernameOrEmailOrNickname(request.getUsername(), request.getEmail(), request.getNickname())) {
            throw new UserAlreadyExistsException();
        }
        userRepository.save(
                User.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .nickname(request.getNickname())
                        .email(request.getEmail())
                        .role("ROLE_USER")
                .build()
        );
    }

    @GetMapping
    public InfoResponse info(@CurrentUser User user) {
        return new InfoResponse(user.getUsername(), user.getEmail(), user.getNickname());
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

}
