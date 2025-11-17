package com.example.demo.service;

import com.example.demo.strategy.auth.Verifiable;

public interface AuthenticationService {
    String register(Verifiable request);
    String login(Verifiable request);
}
