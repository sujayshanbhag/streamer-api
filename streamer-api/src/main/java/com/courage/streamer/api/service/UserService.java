package com.courage.streamer.api.service;

import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import com.courage.streamer.api.model.entity.User;

import java.util.Optional;


public interface UserService {
    User createUser(AuthenticationResult authResult);
    Optional<User> findByEmail(String email);
}
