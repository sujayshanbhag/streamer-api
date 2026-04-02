package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.key-id}")
    private String keyId;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;


    public JwtServiceImpl(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public String generateToken(String username, List<String> scopes, String version) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60 * 60))
                .subject(username)
                .claim("scope", scopes)
                .claim("ver", version)
                .build();

        JwsHeader header = JwsHeader.with(() -> "HS256")
                .keyId(keyId)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60 * 60 * 24 * 7))
                .subject(username)
                .claim("type", "refresh")
                .build();

        JwsHeader header = JwsHeader.with(() -> "HS256")
                .keyId(keyId)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            String tokenType = jwt.getClaimAsString("type");
            Instant expiration = jwt.getExpiresAt();

            // Check if the token type is "refresh" and not expired
            return "refresh".equals(tokenType) && expiration.isAfter(Instant.now());
        } catch (Exception e) {
            log.warn("Refresh token validation failed", e);
            return false; // Invalid token
        }
    }

    @Override
    public String extractUserIdFromRefreshToken(String refreshToken) {
        Jwt jwt = jwtDecoder.decode(refreshToken);
        return jwt.getSubject(); // Extract the user ID from the subject
    }

}

