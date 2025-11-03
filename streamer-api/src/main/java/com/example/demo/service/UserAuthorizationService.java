package com.example.demo.service;

import com.example.demo.model.entity.User;

public interface UserAuthorizationService {
    String authorize(User user);
    boolean validate(Long userId, String version);

}

