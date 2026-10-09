package com.example.authservice.service;

import com.example.authservice.dto.TokenResponse;
import com.example.authservice.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public JwtService(JwtEncoder jwtEncoder, @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Creates a JWT signed with HS256 whose subject is the username, it also carries the userId and roles claims
     * that the other services use for authorization
     * @param user Authenticated user
     * @return The token and its lifetime
     */
    public TokenResponse generateToken(UserDto user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("auth-service")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(user.username())
                .claim("userId", user.id())
                .claim("roles", List.of(user.role()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expirationSeconds);
    }
}
