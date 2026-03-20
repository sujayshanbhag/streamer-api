package com.courage.streamer.api.service;

import com.courage.streamer.api.model.dto.TokenResponseDto;
import com.courage.streamer.common.entity.User;

public interface UserAuthorizationService {
    TokenResponseDto authorize(User user);
    boolean isUserActive(Long userId);
    boolean validate(Long userId, String version);
    boolean validateRefreshToken(String refreshToken);
    TokenResponseDto refreshToken(String refreshToken);
}

