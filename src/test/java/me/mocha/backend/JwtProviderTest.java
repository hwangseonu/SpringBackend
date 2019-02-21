package me.mocha.backend;

import io.jsonwebtoken.Jwts;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.TokenRepository;
import me.mocha.backend.common.security.jwt.JwtProvider;
import me.mocha.backend.common.security.jwt.JwtType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest(JwtProvider.class)
public class JwtProviderTest {

    Logger logger = LoggerFactory.getLogger(JwtProviderTest.class);

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private TokenRepository tokenRepository;

    @Test
    public void testGenerateToken() throws Exception {
        User owner = User.builder().username("test").password("test").build();
        String jwt = jwtProvider.createToken(owner, "Mozilla/5.0", JwtType.ACCESS);
        Jwts.parser().requireSubject(JwtType.ACCESS.toString()).setSigningKey(jwtProvider.getSecret()).parse(jwt);
        logger.info("jwt is " + jwt);
    }

}
