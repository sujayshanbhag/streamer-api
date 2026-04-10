package com.courage.streamer.api.strategy.auth.google;

import com.courage.streamer.api.constant.AuthenticationStatus;
import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import com.courage.streamer.api.strategy.auth.google.GoogleAuthenticationInput;
import com.courage.streamer.api.strategy.auth.google.GoogleAuthenticationStrategy;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleAuthenticationStrategyTest {

    private GoogleIdToken mockIdToken;
    private GoogleAuthenticationStrategy strategy;

    @BeforeEach
    void setUp() {
        mockIdToken = mock(GoogleIdToken.class);
        strategy = new GoogleAuthenticationStrategy("mock-client-id", "mock-client-secret") {
            @Override
            protected GoogleIdToken exchangeCodeForIdToken(String code, String redirectUri) throws Exception {
                if ("valid-code".equals(code)) return mockIdToken;
                if ("invalid-code".equals(code)) return null;
                throw new RuntimeException("Verification error");
            }
        };
    }

    @Test
    void authenticateReturnsSuccessForValidToken() throws Exception {
        GoogleAuthenticationInput input = new GoogleAuthenticationInput("valid-code", "postmessage");

        Payload payload = mock(Payload.class);
        when(mockIdToken.getPayload()).thenReturn(payload);
        when(payload.get("name")).thenReturn("John Doe");
        when(payload.getEmail()).thenReturn("john.doe@example.com");

        AuthenticationResult result = strategy.authenticate(input);

        assertEquals(AuthenticationStatus.SUCCESS, result.getStatus());
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
    }

    @Test
    void authenticateReturnsFailedForInvalidToken() {
        GoogleAuthenticationInput input = new GoogleAuthenticationInput("invalid-code", "postmessage");

        AuthenticationResult result = strategy.authenticate(input);

        assertEquals(AuthenticationStatus.FAILED, result.getStatus());
        assertEquals("Google auth failed", result.getMessage());
    }

    @Test
    void authenticateReturnsFailedForException() {
        GoogleAuthenticationInput input = new GoogleAuthenticationInput("exception-code", "postmessage");

        AuthenticationResult result = strategy.authenticate(input);

        assertEquals(AuthenticationStatus.FAILED, result.getStatus());
        assertEquals("Unknown Google auth error: Verification error", result.getMessage());
    }
}
