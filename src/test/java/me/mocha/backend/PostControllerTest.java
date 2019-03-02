package me.mocha.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.UserRepository;
import me.mocha.backend.common.security.UserDetailsServiceImpl;
import me.mocha.backend.common.security.jwt.JwtAuthenticationEntryPoint;
import me.mocha.backend.common.security.jwt.JwtAuthenticationFilter;
import me.mocha.backend.common.security.jwt.JwtProvider;
import me.mocha.backend.post.controller.PostController;
import me.mocha.backend.post.model.entity.Post;
import me.mocha.backend.post.model.repository.PostRepository;
import me.mocha.backend.post.request.NewPostRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({PostController.class})
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostRepository postRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtProvider jwtProvider;

    @SpyBean
    private UserDetailsServiceImpl userDetailsService;

    @SpyBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @SpyBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    final private String username = "test1234";
    private User user = User.builder().username(username).password(username).nickname(username).email("test@email.com").roles(Collections.singletonList("ROLE_USER")).build();

    @Before
    public void setUp() {
        given(userRepository.findById(username)).willReturn(Optional.of(user));
    }

    @WithMockUser(username = username)
    @Test
    public void createPost_success() throws Exception {
        String title = "test";
        String content = "test";
        Date date = new Date();
        Post post = Post.builder().id(1).title(title).content(content).createAt(date).updateAt(date).views(0).writer(user).build();
        when(postRepository.save(any(Post.class))).thenReturn(post);
        NewPostRequest request = new NewPostRequest(title, content);
        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.writer.username").value(username))
                .andExpect(jsonPath("$.writer.nickname").value(username))
                .andExpect(jsonPath("$.writer.email").value("test@email.com"))
                .andExpect(jsonPath("$.createAt").value(date))
                .andExpect(jsonPath("$.updateAt").value(date))
                .andExpect(jsonPath("$.views").value(0))
                .andDo(print());
    }

}
