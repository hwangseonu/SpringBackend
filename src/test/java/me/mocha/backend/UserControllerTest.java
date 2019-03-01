package me.mocha.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.UserRepository;
import me.mocha.backend.common.security.UserDetailsServiceImpl;
import me.mocha.backend.common.security.UserPrincipal;
import me.mocha.backend.common.security.jwt.JwtProvider;
import me.mocha.backend.common.security.jwt.JwtType;
import me.mocha.backend.user.controller.UserController;
import me.mocha.backend.user.request.SignUpRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = {UserController.class}, secure = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

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
        given(userRepository.findById("test1234")).willReturn(Optional.of(User.builder()
                .username("test1234")
                .password("test1234")
                .nickname("test1234")
                .email("test@email.com")
                .roles(Collections.singletonList("ROLE_USER"))
                .build()));

        mockMvc.perform(get("/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test1234"))
                .andExpect(jsonPath("$.nickname").value("test1234"))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andDo(print());
    }

    @Test
    public void getUserWithJwt_success() throws Exception {
        String jwt = "access";
        String username = "test1234";
        User user = User.builder()
                .username(username)
                .password("test1234")
                .nickname("test1234")
                .email("test@email.com")
                .roles(Collections.singletonList("ROLE_USER"))
                .build();
        given(userRepository.findById(username)).willReturn(Optional.of(user));
        given(userRepository.existsById(username)).willReturn(true);
        given(userDetailsService.loadUserByUsername(username)).willReturn(UserPrincipal.create(user));
        given(jwtProvider.getUsername(jwt)).willReturn(username);
        given(jwtProvider.isValid(jwt, JwtType.ACCESS)).willReturn(true);

        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + jwt)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test1234"))
                .andExpect(jsonPath("$.nickname").value("test1234"))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andDo(print());
    }

}
