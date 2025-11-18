package com.example.streamer.api.service;

import com.example.streamer.api.model.entity.User;

public interface UserAuthorizationService {
    String authorize(User user);
    boolean isUserActive(Long userId);
    boolean validate(Long userId, String version);

}

