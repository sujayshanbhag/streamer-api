package com.courage.streamer.api.strategy.auth;

import com.courage.streamer.api.constant.AuthenticationType;
import com.courage.streamer.api.strategy.auth.AuthenticationStrategy;
import com.courage.streamer.api.strategy.auth.AuthenticationStrategyProvider;
import com.courage.streamer.api.strategy.auth.github.GithubAuthenticationStrategy;
import com.courage.streamer.api.strategy.auth.google.GoogleAuthenticationStrategy;
import com.courage.streamer.api.strategy.auth.otp.OtpAuthenticationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AuthenticationStrategyProviderTest {

    private AuthenticationStrategyProvider provider;
    private GoogleAuthenticationStrategy googleStrategy;
    private GithubAuthenticationStrategy githubStrategy;
    private OtpAuthenticationStrategy otpStrategy;

    @BeforeEach
    void setUp() {
        googleStrategy = mock(GoogleAuthenticationStrategy.class);
        githubStrategy = mock(GithubAuthenticationStrategy.class);
        otpStrategy = mock(OtpAuthenticationStrategy.class);
        provider = new AuthenticationStrategyProvider(googleStrategy, githubStrategy, otpStrategy);
    }

    @Test
    void getStrategyReturnsGoogleStrategyForGoogleType() {
        AuthenticationStrategy strategy = provider.getStrategy(AuthenticationType.GOOGLE);
        assertEquals(googleStrategy, strategy);
    }

    @Test
    void getStrategyReturnsGithubStrategyForGithubType() {
        AuthenticationStrategy strategy = provider.getStrategy(AuthenticationType.GITHUB);
        assertEquals(githubStrategy, strategy);
    }

    @Test
    void getStrategyReturnsOtpStrategyForOtpType() {
        AuthenticationStrategy strategy = provider.getStrategy(AuthenticationType.OTP);
        assertEquals(otpStrategy, strategy);
    }

}