package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class JwtServiceImplTest {

    private final JwtEncoder jwtEncoder = Mockito.mock(JwtEncoder.class);
    private final JwtDecoder jwtDecoder = Mockito.mock(JwtDecoder.class);
    private final JwtServiceImpl jwtService = new JwtServiceImpl(jwtEncoder, jwtDecoder);

    @Test
    void testGenerateToken() {
        ReflectionTestUtils.setField(jwtService, "keyId", "test-key-id");
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

    @Test
    void testGenerateRefreshToken() {
        // Set the keyId value
        ReflectionTestUtils.setField(jwtService, "keyId", "test-key-id");

        // Mock the JwtEncoder to return a dummy token
        Jwt mockJwt = Mockito.mock(Jwt.class);
        Mockito.when(mockJwt.getTokenValue()).thenReturn("dummy-refresh-token");
        Mockito.when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        // Call the method under test
        String refreshToken = jwtService.generateRefreshToken("testUser");

        // Verify the token value
        assertThat(refreshToken).isEqualTo("dummy-refresh-token");

        // Verify that the JwtEncoder was called with the correct parameters
        Mockito.verify(jwtEncoder).encode(Mockito.argThat(params -> {
            JwtClaimsSet claims = params.getClaims();
            return claims.getSubject().equals("testUser")
                    && claims.getClaim("type").equals("refresh")
                    && claims.getExpiresAt().isAfter(Instant.now());
        }));
    }
}
