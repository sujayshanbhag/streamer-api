package com.courage.streamer.api.service;

import com.courage.streamer.api.model.entity.User;

public interface UserAuthorizationService {
    String authorize(User user);
    boolean isUserActive(Long userId);
    boolean validate(Long userId, String version);

}

