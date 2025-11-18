package com.example.streamer.api.service;

import java.util.List;

public interface JwtService {
    public String generateToken(String username, List<String> scopes, String version);
}
