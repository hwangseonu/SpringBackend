package me.mocha.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.UserRepository;
import me.mocha.backend.common.security.UserDetailsServiceImpl;
import me.mocha.backend.common.security.jwt.JwtAuthenticationEntryPoint;
import me.mocha.backend.common.security.jwt.JwtAuthenticationFilter;
import me.mocha.backend.common.security.jwt.JwtProvider;
import me.mocha.backend.common.security.jwt.JwtType;
import me.mocha.backend.user.controller.UserController;
import me.mocha.backend.user.request.SignUpRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = {UserController.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @SpyBean
    private UserDetailsServiceImpl userDetailsService;

    @SpyBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @SpyBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void signUp_success() throws Exception {
        String username = "test1234";
        String email = "test@email.com";
        SignUpRequest request = new SignUpRequest(username, username, username, email);
        User user = User.builder().username(username).password(passwordEncoder.encode(username)).nickname(username).email(email).roles(Collections.singletonList("ROLE_USER")).build();
        when(userRepository.save(any(User.class))).thenReturn(user);
        given(jwtProvider.createToken(username, JwtType.ACCESS)).willReturn("access");
        given(jwtProvider.createToken(username, JwtType.REFRESH)).willReturn("refresh");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access").value("access"))
                .andExpect(jsonPath("$.refresh").value("refresh"))
                .andDo(print());
    }

    @Test
    public void signUp_conflict() throws Exception {
        String username = "test1234";
        String email = "test@email.com";
        SignUpRequest request = new SignUpRequest(username, username, username, email);
        given(userRepository.existsByUsernameOrNicknameOrEmail(username, username, email)).willReturn(true);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    public void signUp_badRequest() throws Exception {
        SignUpRequest request = new SignUpRequest("tes", "test123", "test1234", "test");
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @WithMockUser(username = "test1234")
    @Test
    public void getUser_success() throws Exception {
        String username = "test1234";
        String email = "test@email.com";
        User user = User.builder().username(username).password(passwordEncoder.encode(username)).nickname(username).email(email).roles(Collections.singletonList("ROLE_USER")).build();
        given(userRepository.findById(username)).willReturn(Optional.of(user));

        mockMvc.perform(get("/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test1234"))
                .andExpect(jsonPath("$.nickname").value("test1234"))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andDo(print());
    }

    @WithMockUser(username = "test1234")
    @Test
    public void getUserWithJwt_success() throws Exception {
        String username = "test1234";
        String email = "test@email.com";
        String jwt = "access";
        User user = User.builder().username(username).password(passwordEncoder.encode(username)).nickname(username).email(email).roles(Collections.singletonList("ROLE_USER")).build();
        given(userRepository.existsById(username)).willReturn(true);
        given(userRepository.findById(username)).willReturn(Optional.of(user));
        given(jwtProvider.isValid(jwt, JwtType.ACCESS)).willReturn(true);
        given(jwtProvider.getUsername(jwt)).willReturn(username);

        mockMvc.perform(get("/users").accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test1234"))
                .andExpect(jsonPath("$.nickname").value("test1234"))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andDo(print());
    }

    @Test
    public void getUserWithJwt_notfound() throws Exception {
        String username = "test1234";
        String jwt = "access";
        given(userRepository.existsById(username)).willReturn(false);
        given(jwtProvider.isValid(jwt, JwtType.ACCESS)).willReturn(true);
        given(jwtProvider.getUsername(jwt)).willReturn(username);

        mockMvc.perform(get("/users").accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void getUserWithJwt_unprocessableEntity() throws Exception {
        String jwt = "access";
        given(jwtProvider.isValid(jwt, JwtType.ACCESS)).willReturn(false);

        mockMvc.perform(get("/users").accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + jwt))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

}
