package com.example.streamer.api.service;

import com.example.streamer.api.strategy.auth.Verifiable;

public interface AuthenticationService {
    String register(Verifiable request);
    String login(Verifiable request);
}
