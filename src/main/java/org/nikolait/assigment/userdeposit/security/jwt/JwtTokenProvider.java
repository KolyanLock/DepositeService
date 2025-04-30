package org.nikolait.assigment.userdeposit.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private Algorithm algorithm;
    private Duration duration;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        this.duration = Duration.ofMinutes(jwtProperties.getMinutes());
    }

    public String generateToken(Long userId) {
        Date now = new Date();
        return JWT.create()
                .withClaim(jwtProperties.getClaim(), userId)
                .withIssuedAt(now)
                .withExpiresAt(now.toInstant().plus(duration))
                .sign(algorithm);
    }

    public Long extractUserId(String token) {
        DecodedJWT  jwt = JWT.require(algorithm).build().verify(token);
        Long userId = jwt.getClaim(jwtProperties.getClaim()).asLong();
        if (userId == null) {
            throw new JWTDecodeException("Invalid token claim: " + jwtProperties.getClaim());
        }
        return userId;
    }
}

