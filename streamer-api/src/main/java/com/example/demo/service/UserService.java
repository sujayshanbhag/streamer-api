package com.example.demo.service;

import com.example.demo.model.entity.User;
import com.example.demo.strategy.auth.AuthenticationResult;

import java.util.Optional;


public interface UserService {
    User createUser(AuthenticationResult authResult);
    Optional<User> findByEmail(String email);
}
