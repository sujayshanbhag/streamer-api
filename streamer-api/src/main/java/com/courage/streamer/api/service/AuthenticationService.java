package com.courage.streamer.api.service;

import com.courage.streamer.api.strategy.auth.Verifiable;

public interface AuthenticationService {
    String register(Verifiable request);
    String login(Verifiable request);
}
