package com.example.streamer.api.service;

import com.example.streamer.api.model.entity.User;
import com.example.streamer.api.strategy.auth.AuthenticationResult;

import java.util.Optional;


public interface UserService {
    User createUser(AuthenticationResult authResult);
    Optional<User> findByEmail(String email);
}
