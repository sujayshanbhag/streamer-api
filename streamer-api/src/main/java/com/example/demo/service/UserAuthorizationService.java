package com.example.demo.service;

import com.example.demo.model.entity.User;

public interface UserAuthorizationService {
    String authorize(User user);
    boolean isUserActive(Long userId);
    boolean validate(Long userId, String version);

}

