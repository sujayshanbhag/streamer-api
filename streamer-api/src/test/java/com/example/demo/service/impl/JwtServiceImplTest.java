package com.example.demo.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class JwtServiceImplTest {

    private final JwtEncoder jwtEncoder = Mockito.mock(JwtEncoder.class);
    private final JwtServiceImpl jwtService = new JwtServiceImpl(jwtEncoder);

    @Test
    void testGenerateToken() {
        // Mock the JwtEncoder to return a dummy token
        Jwt mockJwt = Mockito.mock(Jwt.class);
        Mockito.when(mockJwt.getTokenValue()).thenReturn("dummy-token");
        Mockito.when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        // Call the method under test
        String token = jwtService.generateToken("testUser", List.of("read", "write"), "1.0");

        // Verify the token value
        assertThat(token).isEqualTo("dummy-token");

        // Verify that the JwtEncoder was called with the correct parameters
        Mockito.verify(jwtEncoder).encode(Mockito.argThat(params -> {
            JwtClaimsSet claims = params.getClaims();
            return claims.getSubject().equals("testUser")
                    && claims.getClaim("scope").equals(List.of("read", "write"))
                    && claims.getClaim("ver").equals("1.0")
                    && claims.getExpiresAt().isAfter(Instant.now());
        }));
    }
}
