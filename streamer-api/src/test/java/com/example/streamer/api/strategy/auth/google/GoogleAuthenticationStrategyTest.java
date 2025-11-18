package com.example.streamer.api.strategy.auth.google;

import com.example.streamer.api.constant.AuthenticationStatus;
import com.example.streamer.api.strategy.auth.AuthenticationResult;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleAuthenticationStrategyTest {

    private GoogleIdTokenVerifier verifier;
    private GoogleAuthenticationStrategy strategy;

    @BeforeEach
    void setUp() throws Exception {
        verifier = mock(GoogleIdTokenVerifier.class);
        strategy = new GoogleAuthenticationStrategy("mock-client-id");

        // Use reflection to set the private final field
        Field verifierField = GoogleAuthenticationStrategy.class.getDeclaredField("verifier");
        verifierField.setAccessible(true);
        verifierField.set(strategy, verifier);
    }

    @Test
    void authenticateReturnsSuccessForValidToken() throws Exception {
        GoogleAuthenticationInput input = new GoogleAuthenticationInput("valid-token");
        GoogleIdToken idToken = mock(GoogleIdToken.class);
        Payload payload = mock(Payload.class);

        when(verifier.verify("valid-token")).thenReturn(idToken);
        when(idToken.getPayload()).thenReturn(payload);
        when(payload.get("name")).thenReturn("John Doe");
        when(payload.getEmail()).thenReturn("john.doe@example.com");

        AuthenticationResult result = strategy.authenticate(input);

        assertEquals(AuthenticationStatus.SUCCESS, result.getStatus());
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
    }

    @Test
    void authenticateReturnsFailedForInvalidToken() throws Exception {
        GoogleAuthenticationInput input = new GoogleAuthenticationInput("invalid-token");

        when(verifier.verify("invalid-token")).thenReturn(null);

        AuthenticationResult result = strategy.authenticate(input);

        assertEquals(AuthenticationStatus.FAILED, result.getStatus());
        assertEquals("Google auth failed", result.getMessage());
    }

    @Test
    void authenticateReturnsFailedForException() throws Exception {
        GoogleAuthenticationInput input = new GoogleAuthenticationInput("exception-token");

        when(verifier.verify("exception-token")).thenThrow(new RuntimeException("Verification error"));

        AuthenticationResult result = strategy.authenticate(input);

        assertEquals(AuthenticationStatus.FAILED, result.getStatus());
        assertEquals("Unknown Google auth error", result.getMessage());
    }
}