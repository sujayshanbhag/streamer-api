package com.example.demo.strategy.auth;


import com.example.demo.constant.AuthenticationType;
import com.example.demo.strategy.auth.github.GithubAuthenticationStrategy;
import com.example.demo.strategy.auth.google.GoogleAuthenticationStrategy;
import com.example.demo.strategy.auth.otp.OtpAuthenticationStrategy;
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
