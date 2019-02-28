package me.mocha.backend;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import me.mocha.backend.common.model.repository.UserRepository;
import me.mocha.backend.common.security.UserDetailsServiceImpl;
import me.mocha.backend.common.security.jwt.JwtProvider;
import me.mocha.backend.common.security.jwt.JwtType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest(value = {JwtProvider.class}, secure = false)
public class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    public void createToken_success() throws JwtException {
        String jwt = jwtProvider.createToken("test1234", JwtType.ACCESS);
        Jwts.parser()
                .requireSubject(JwtType.ACCESS.toString())
                .require("identity", "test1234")
                .setSigningKey(jwtProvider.getSecret())
                .parseClaimsJws(jwt);
    }

    @Test
    public void isValid_false() {
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        Assert.assertFalse(jwtProvider.isValid(jwt, JwtType.ACCESS));
    }

    @Test
    public void isValid_true() {
        String jwt = jwtProvider.createToken("test1234", JwtType.ACCESS);
        Assert.assertTrue(jwtProvider.isValid(jwt, JwtType.ACCESS));
    }

}
