package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.dto.TokenResponseDto;
import com.courage.streamer.api.exception.AuthenticationException;
import com.courage.streamer.api.exception.ForbiddenException;
import com.courage.streamer.common.entity.User;
import com.courage.streamer.common.repository.UserRepository;
import com.courage.streamer.api.service.JwtService;
import com.courage.streamer.api.service.UserAuthorizationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserAuthorizationServiceImpl implements UserAuthorizationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public UserAuthorizationServiceImpl(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public TokenResponseDto authorize(User user) {
        List<String> scopes = user.getPermissions().stream()
                                  .map((permission) -> permission.getType().toString())
                                  .toList();
        String accessToken = jwtService.generateToken(user.getId().toString(), scopes, user.getVersion());
        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public boolean isUserActive(Long userId) {
        return userRepository.findById(userId)
                .map(User::getIsActive)
                .orElse(false);
    }

    @Override
    public boolean validate(Long userId, String version) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            return user.getVersion().equals(version);
        }
        return false;
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        return jwtService.validateRefreshToken(refreshToken);
    }

    @Override
    public TokenResponseDto refreshToken(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        String userId = jwtService.extractUserIdFromRefreshToken(refreshToken);
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!user.getIsActive()) {
            throw new ForbiddenException("Forbidden user");
        }

        List<String> scopes = user.getPermissions().stream()
                .map(permission -> permission.getType().toString())
                .toList();
        String accessToken = jwtService.generateToken(user.getId().toString(), scopes, user.getVersion());

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken) // Return the same refresh token
                .build();
    }
}

