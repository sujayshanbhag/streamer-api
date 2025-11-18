package com.courage.streamer.api.strategy.auth;

public interface AuthenticationStrategy {
    AuthenticationResult validateAndAuthenticate(Verifiable input);
}
