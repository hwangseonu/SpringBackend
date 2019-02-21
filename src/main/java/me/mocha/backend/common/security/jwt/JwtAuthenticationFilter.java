package me.mocha.backend.common.security.jwt;

import me.mocha.backend.common.model.entity.Token;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.common.model.repository.TokenRepository;
import me.mocha.backend.common.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtProvider jwtProvider, TokenRepository tokenRepository, UserDetailsServiceImpl userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.tokenRepository = tokenRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = getJwtFromHeader(request);
        if (StringUtils.hasText(jwt)) {
            if (jwtProvider.validToken(jwt, JwtType.ACCESS)) {
                String id = jwtProvider.getId(jwt, JwtType.ACCESS);
                Token token = tokenRepository.findById(UUID.fromString(id)).orElse(null);

                if (token == null) {
                    response.sendError(422);
                    return;
                }

                User user = userDetailsService.loadUserByUsername(token.getOwner().getUsername());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                response.sendError(422);
            }
        } else {
            response.sendError(401);
        }
    }

    private String getJwtFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer")) {
            return header.replaceFirst("Bearer", "").trim();
        }
        return "";
    }

}
