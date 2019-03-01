package me.mocha.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.UserRepository;
import me.mocha.backend.common.security.UserDetailsServiceImpl;
import me.mocha.backend.common.security.jwt.JwtProvider;
import me.mocha.backend.common.security.jwt.JwtType;
import me.mocha.backend.user.controller.AuthController;
import me.mocha.backend.user.request.SignInRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {AuthController.class}, secure = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void signIn_success() throws Exception {
        given(userRepository.findById("test1234")).willReturn(Optional.of(User.builder()
                .username("test1234")
                .password("test1234")
                .nickname("test1234")
                .email("test@email.com")
                .build()));
        given(jwtProvider.createToken("test1234", JwtType.ACCESS)).willReturn("access");
        given(jwtProvider.createToken("test1234", JwtType.REFRESH)).willReturn("refresh");
        given(passwordEncoder.matches("test1234", "test1234")).willReturn(true);

        SignInRequest request = new SignInRequest("test1234", "test1234");
        mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access").value("access"))
                .andExpect(jsonPath("$.refresh").value("refresh"))
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
        given(userRepository.findById("test1234")).willReturn(Optional.of(User.builder()
                .username("test1234")
                .password("test1234")
                .nickname("test1234")
                .email("test@email.com")
                .build()));
        given(passwordEncoder.matches("test1234", "test1111")).willReturn(false);
        SignInRequest request = new SignInRequest("test1234", "test1111");
        mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
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
        String jwt = "access";
        given(jwtProvider.isValid(jwt, JwtType.REFRESH)).willReturn(false);
        mockMvc.perform(get("/auth/refresh")
                .header("Authorization", "Bearer " + jwt)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    public void refresh_notfound() throws Exception {
        String jwt = "refresh";
        String username = "test1234";
        given(jwtProvider.isValid(jwt, JwtType.REFRESH)).willReturn(true);
        given(jwtProvider.getUsername(jwt)).willReturn(username);
        given(userRepository.existsById(username)).willReturn(false);

        mockMvc.perform(get("/auth/refresh")
                .header("Authorization", "Bearer " + jwt)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void refresh_success() throws Exception {
        String jwt = "refresh";
        String username = "test1234";

        given(jwtProvider.isValid(jwt, JwtType.REFRESH)).willReturn(true);
        given(jwtProvider.getUsername(jwt)).willReturn(username);
        given(jwtProvider.createToken(username, JwtType.ACCESS)).willReturn("access");
        given(jwtProvider.createToken(username, JwtType.REFRESH)).willReturn("refresh");
        given(jwtProvider.getExpiration(jwt)).willReturn(LocalDateTime.now().plusDays(6));
        given(userRepository.existsById(username)).willReturn(true);

        mockMvc.perform(get("/auth/refresh")
                .header("Authorization", "Bearer " + jwt)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access").value("access"))
                .andExpect(jsonPath("$.refresh").value("refresh"))
                .andDo(print());
    }

}
