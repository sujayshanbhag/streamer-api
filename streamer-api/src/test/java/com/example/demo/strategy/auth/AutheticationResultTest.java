package com.example.demo.strategy.auth;

import com.example.demo.constant.AuthenticationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationResultTest {

    @Test
    void failedCreatesAuthenticationResultWithFailedStatusAndMessage() {
        String errorMessage = "Authentication failed";

        AuthenticationResult result = AuthenticationResult.failed(errorMessage);

        assertEquals(AuthenticationStatus.FAILED, result.getStatus());
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getEmail());
        assertNull(result.getName());
    }

    @Test
    void buildSetsStatusToFailedWhenEmailIsMissing() {
        AuthenticationResult result = AuthenticationResult.builder()
                .name("John Doe")
                .status(AuthenticationStatus.SUCCESS)
                .build();

        assertEquals(AuthenticationStatus.FAILED, result.getStatus());
        assertEquals("Email not found", result.getMessage());
    }

    @Test
    void buildCreatesAuthenticationResultWithAllFields() {
        AuthenticationResult result = AuthenticationResult.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .pictureUrl("http://example.com/picture.jpg")
                .status(AuthenticationStatus.SUCCESS)
                .build();

        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhoneNumber());
        assertEquals("http://example.com/picture.jpg", result.getPictureUrl());
        assertEquals(AuthenticationStatus.SUCCESS, result.getStatus());
    }
}