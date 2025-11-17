package com.example.demo.service.impl;

import com.example.demo.constant.AuthenticationStatus;
import com.example.demo.constant.AuthenticationType;
import com.example.demo.exception.AuthenticationException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.model.entity.User;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.UserService;
import com.example.demo.strategy.auth.AuthenticationResult;
import com.example.demo.strategy.auth.AuthenticationStrategy;
import com.example.demo.strategy.auth.AuthenticationStrategyProvider;
import com.example.demo.strategy.auth.Verifiable;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationStrategyProvider strategyProvider;
    private final UserService userService;

    private final UserAuthorizationServiceImpl userAuthorizationService;
    public AuthenticationServiceImpl(AuthenticationStrategyProvider strategyProvider, UserService userService, UserAuthorizationServiceImpl userAuthorizationService) {
        this.strategyProvider = strategyProvider;
        this.userService = userService;
        this.userAuthorizationService = userAuthorizationService;
    }

    @Override
    public String register(Verifiable request) {
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
    public String login(Verifiable request) {
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
}
