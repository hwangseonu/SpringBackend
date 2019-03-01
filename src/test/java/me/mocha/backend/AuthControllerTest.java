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

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(jsonPath("$.refresh").value("refresh"));
    }

    @Test
    public void signIn_notfound() throws Exception {
        SignInRequest request = new SignInRequest("test1234", "test1234");
        mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
                .andExpect(status().isUnauthorized());
    }

}
