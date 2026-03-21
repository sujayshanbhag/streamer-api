package com.courage.streamer.api.service;

import com.courage.streamer.api.dto.TokenResponseDto;
import com.courage.streamer.api.strategy.auth.Verifiable;

public interface AuthenticationService {
    TokenResponseDto register(Verifiable request);
    TokenResponseDto login(Verifiable request);
    TokenResponseDto refresh(String refreshToken);
}
