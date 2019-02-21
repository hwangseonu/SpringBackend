package me.mocha.backend;

import io.jsonwebtoken.Jwts;
import me.mocha.backend.common.model.repository.TokenRepository;
import me.mocha.backend.common.security.UserDetailsServiceImpl;
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

import java.util.UUID;

@RunWith(SpringRunner.class)
@WebMvcTest(value = JwtProvider.class, secure = false)
public class JwtProviderTest {

    Logger logger = LoggerFactory.getLogger(JwtProviderTest.class);

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private TokenRepository tokenRepository;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    public void testGenerateToken() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String jwt = jwtProvider.createToken(uuid, JwtType.ACCESS);
        Jwts.parser().requireSubject(JwtType.ACCESS.toString()).setSigningKey(jwtProvider.getSecret()).parse(jwt);
        Jwts.parser().requireSubject(JwtType.ACCESS.toString()).requireId(uuid).parseClaimsJwt(jwt);
        logger.info("jwt is " + jwt);
    }

}
