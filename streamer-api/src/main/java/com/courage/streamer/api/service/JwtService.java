package com.courage.streamer.api.service;

import java.util.List;

public interface JwtService {
    String generateToken(String username, List<String> scopes, String version);
    String generateRefreshToken(String username);
    boolean validateRefreshToken(String refreshToken);
    String extractUserIdFromRefreshToken(String refreshToken);
}
