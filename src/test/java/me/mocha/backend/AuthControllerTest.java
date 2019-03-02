package me.mocha.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.UserRepository;
import me.mocha.backend.common.security.UserDetailsServiceImpl;
import me.mocha.backend.common.security.jwt.JwtAuthenticationEntryPoint;
import me.mocha.backend.common.security.jwt.JwtAuthenticationFilter;
import me.mocha.backend.common.security.jwt.JwtProvider;
import me.mocha.backend.common.security.jwt.JwtType;
import me.mocha.backend.user.controller.AuthController;
import me.mocha.backend.user.request.SignInRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
@WebMvcTest(value = {AuthController.class})
public class AuthControllerTest {

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
    public void signIn_success() throws Exception {
        String username = "test1234";
        String email = "test@email.com";
        String access = "access";
        String refresh = "refresh";
        User user = User.builder().username(username).password(passwordEncoder.encode(username)).nickname(username).email(email).build();
        given(userRepository.findById(username)).willReturn(Optional.of(user));
        given(jwtProvider.createToken(username, JwtType.ACCESS)).willReturn(access);
        given(jwtProvider.createToken(username, JwtType.REFRESH)).willReturn(refresh);

        SignInRequest request = new SignInRequest(username, username);
        mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access").value(access))
                .andExpect(jsonPath("$.refresh").value(refresh))
                .andDo(print());
    }

    @Test
    public void signIn_notfound() throws Exception {
        SignInRequest request = new SignInRequest("test1234", "test1234");
        mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void signIn_unauthorized() throws Exception {
        String username = "test1234";
        String email = "test@email.com";
        String access = "access";
        String refresh = "refresh";
        User user = User.builder().username(username).password(passwordEncoder.encode(username)).nickname(username).email(email).build();
        given(userRepository.findById(username)).willReturn(Optional.of(user));
        given(jwtProvider.createToken(username, JwtType.ACCESS)).willReturn(access);
        given(jwtProvider.createToken(username, JwtType.REFRESH)).willReturn(refresh);

        SignInRequest request = new SignInRequest(username, "test1111");
        mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void signIn_badRequest() throws Exception {
        SignInRequest request = new SignInRequest("a", "a");
        mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void refresh_success() throws Exception {
        String access = "access";
        String refresh = "refresh";
        String username = "username";

        given(jwtProvider.createToken(username, JwtType.ACCESS)).willReturn(access);
        given(jwtProvider.createToken(username, JwtType.REFRESH)).willReturn(refresh);
        given(jwtProvider.isValid(refresh, JwtType.REFRESH)).willReturn(true);
        given(jwtProvider.getExpiration(refresh)).willReturn(LocalDateTime.now().plusDays(6));
        when(jwtProvider.getUsername(any())).thenReturn(username);
        given(userRepository.existsById(username)).willReturn(true);

        mockMvc.perform(get("/auth/refresh")
                .header("Authorization", "Bearer " + refresh)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access").value(access))
                .andExpect(jsonPath("$.refresh").value(refresh))
                .andDo(print());
    }

    @Test
    public void refresh_badRequest() throws Exception {
        mockMvc.perform(get("/auth/refresh")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void refresh_unprocessableEntity() throws Exception {
        String refresh = "refresh";
        String username = "username";

        given(jwtProvider.createToken(username, JwtType.REFRESH)).willReturn(refresh);
        given(jwtProvider.isValid(refresh, JwtType.REFRESH)).willReturn(false);

        mockMvc.perform(get("/auth/refresh")
                .header("Authorization", "Bearer " + refresh)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());

        mockMvc.perform(get("/auth/refresh")
                .header("Authorization", "Bearer ")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    public void refresh_notfound() throws Exception {
        String refresh = "refresh";
        String username = "username";

        given(jwtProvider.createToken(username, JwtType.REFRESH)).willReturn(refresh);
        given(jwtProvider.isValid(refresh, JwtType.REFRESH)).willReturn(true);
        when(jwtProvider.getUsername(any())).thenReturn(username);
        given(userRepository.existsById(username)).willReturn(false);

        mockMvc.perform(get("/auth/refresh")
                .header("Authorization", "Bearer " + refresh)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

}
