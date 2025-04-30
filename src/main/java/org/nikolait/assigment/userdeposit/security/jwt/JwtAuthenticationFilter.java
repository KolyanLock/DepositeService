package org.nikolait.assigment.userdeposit.security.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static org.nikolait.assigment.userdeposit.security.jwt.JwtConstants.BEARER_PREFIX;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            try {
                Long userId = tokenProvider.extractUserId(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JWTVerificationException e) {
                log.warn("JWT verification failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}