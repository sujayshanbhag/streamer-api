package com.courage.streamer.api.strategy.auth;


import com.courage.streamer.api.constant.AuthenticationType;
import com.courage.streamer.api.strategy.auth.github.GithubAuthenticationStrategy;
import com.courage.streamer.api.strategy.auth.otp.OtpAuthenticationStrategy;
import com.courage.streamer.api.strategy.auth.google.GoogleAuthenticationStrategy;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationStrategyProvider {

    private final GoogleAuthenticationStrategy googleAuthenticationStrategy;
    private final GithubAuthenticationStrategy githubAuthenticationStrategy;
    private final OtpAuthenticationStrategy otpAuthenticationStrategy;


    public AuthenticationStrategyProvider(GoogleAuthenticationStrategy googleAuthenticationStrategy, GithubAuthenticationStrategy githubAuthenticationStrategy, OtpAuthenticationStrategy otpAuthenticationStrategy) {
        this.googleAuthenticationStrategy = googleAuthenticationStrategy;
        this.githubAuthenticationStrategy = githubAuthenticationStrategy;
        this.otpAuthenticationStrategy = otpAuthenticationStrategy;
    }

    public AuthenticationStrategy getStrategy(AuthenticationType type) {
        return switch (type) {
            case GOOGLE -> googleAuthenticationStrategy;
            case GITHUB -> githubAuthenticationStrategy;
            case OTP -> otpAuthenticationStrategy;
            default -> throw new IllegalArgumentException("Unsupported auth type: " + type);
        };
    }

}
