package com.example.demo.strategy.auth;

public interface AuthenticationStrategy {
    AuthenticationResult validateAndAuthenticate(Verifiable input);
}
