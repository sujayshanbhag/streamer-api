package com.courage.streamer.api.strategy.auth.otp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpAuthenticationStrategyTest {

    private final OtpAuthenticationStrategy strategy = new OtpAuthenticationStrategy();

    @Test
    void authenticateThrowsUnsupportedOperationException() {
        OtpAuthenticationInput input = new OtpAuthenticationInput();
        input.setEmail("user@example.com");
        input.setOtpCode("123456");

        assertThrows(UnsupportedOperationException.class, () -> strategy.authenticate(input));
    }
}
