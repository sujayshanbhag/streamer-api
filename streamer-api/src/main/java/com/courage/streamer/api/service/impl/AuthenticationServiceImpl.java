package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.constant.AuthenticationStatus;
import com.courage.streamer.api.constant.AuthenticationType;
import com.courage.streamer.api.dto.TokenResponseDto;
import com.courage.streamer.api.service.JwtService;
import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import com.courage.streamer.api.strategy.auth.AuthenticationStrategy;
import com.courage.streamer.api.strategy.auth.Verifiable;
import com.courage.streamer.api.exception.AuthenticationException;
import com.courage.streamer.api.exception.ForbiddenException;
import com.courage.streamer.api.service.AuthenticationService;
import com.courage.streamer.api.service.UserService;
import com.courage.streamer.api.strategy.auth.AuthenticationStrategyProvider;
import com.courage.streamer.common.entity.User;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationStrategyProvider strategyProvider;
    private final UserService userService;

    private final JwtService jwtService;

    private final UserAuthorizationServiceImpl userAuthorizationService;
    public AuthenticationServiceImpl(AuthenticationStrategyProvider strategyProvider, UserService userService, JwtService jwtService, UserAuthorizationServiceImpl userAuthorizationService) {
        this.strategyProvider = strategyProvider;
        this.userService = userService;
        this.jwtService = jwtService;
        this.userAuthorizationService = userAuthorizationService;
    }

    @Override
    public TokenResponseDto register(Verifiable request) {
        AuthenticationType type = request.getType();
        AuthenticationStrategy strategy = strategyProvider.getStrategy(type);

        AuthenticationResult result = strategy.validateAndAuthenticate(request);
        if (result.getStatus() == AuthenticationStatus.FAILED) {
            throw new AuthenticationException(result.getMessage());
        }

        User user = userService.createUser(result);
        return userAuthorizationService.authorize(user);
    }

    @Override
    public TokenResponseDto login(Verifiable request) {
        AuthenticationType type = request.getType();
        AuthenticationStrategy strategy = strategyProvider.getStrategy(type);

        AuthenticationResult result = strategy.validateAndAuthenticate(request);
        if (result.getStatus() == AuthenticationStatus.FAILED) {
            throw new AuthenticationException(result.getMessage());
        }

        User user = userService.findByEmail(result.getEmail())
                .orElseThrow(() -> new AuthenticationException("User does not exist"));
        if(!user.getIsActive()) {
            throw new ForbiddenException("Forbidden user");
        }
        return userAuthorizationService.authorize(user);
    }

    @Override
    public TokenResponseDto refresh(String refreshToken) {
        if (!userAuthorizationService.validateRefreshToken(refreshToken)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        String userId = jwtService.extractUserIdFromRefreshToken(refreshToken);
        User user = userService.findById(Long.valueOf(userId));
        if (!user.getIsActive()) {
            throw new ForbiddenException("Forbidden user");
        }

        return userAuthorizationService.refreshToken(refreshToken);
    }
}
