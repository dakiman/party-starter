package com.example.partystarter.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {

    private static final Logger log = LoggerFactory.getLogger(JWTUtil.class);
    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${application.security.jwt-secret}")
    private String secret;

    @Value("${application.security.issuer}")
    private String issuer;

    private static final String CLAIM = "username";

    @PostConstruct
    void verifySecret() {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least " + MIN_SECRET_LENGTH + " characters (got "
                            + (secret == null ? "null" : secret.length()) + ")");
        }
        log.info("JWT secret loaded ({} chars), issuer={}", secret.length(), issuer);
    }

    public String generateToken(String username) throws IllegalArgumentException, JWTCreationException {
        return JWT.create()
                .withSubject("User Details")
                .withClaim(CLAIM, username)
                .withIssuedAt(new Date())
                .withIssuer(issuer)
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndRetrieveSubject(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User Details")
                .withIssuer(issuer)
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim(CLAIM).asString();
    }

}