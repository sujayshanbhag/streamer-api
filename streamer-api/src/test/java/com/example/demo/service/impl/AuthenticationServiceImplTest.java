package com.example.demo.service.impl;

import com.example.demo.constant.AuthenticationStatus;
import com.example.demo.constant.AuthenticationType;
import com.example.demo.exception.AuthenticationException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.model.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.strategy.auth.AuthenticationResult;
import com.example.demo.strategy.auth.AuthenticationStrategy;
import com.example.demo.strategy.auth.AuthenticationStrategyProvider;
import com.example.demo.strategy.auth.Verifiable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    private AuthenticationStrategyProvider strategyProvider;
    private UserService userService;
    private UserAuthorizationServiceImpl userAuthorizationService;
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        strategyProvider = mock(AuthenticationStrategyProvider.class);
        userService = mock(UserService.class);
        userAuthorizationService = mock(UserAuthorizationServiceImpl.class);
        authenticationService = new AuthenticationServiceImpl(strategyProvider, userService, userAuthorizationService);
    }

    @Test
    void registerSuccessfullyRegistersUser() {
        Verifiable request = mock(Verifiable.class);
        AuthenticationStrategy strategy = mock(AuthenticationStrategy.class);
        AuthenticationResult result = AuthenticationResult.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .status(AuthenticationStatus.SUCCESS)
                .build();
        User user = new User();
        user.setId(1L);

        when(request.getType()).thenReturn(AuthenticationType.OTP);
        when(strategyProvider.getStrategy(AuthenticationType.OTP)).thenReturn(strategy);
        when(strategy.validateAndAuthenticate(request)).thenReturn(result);
        when(userService.createUser(result)).thenReturn(user);
        when(userAuthorizationService.authorize(user)).thenReturn("mockToken");

        String token = authenticationService.register(request);

        assertEquals("mockToken", token);
        verify(strategyProvider).getStrategy(AuthenticationType.OTP);
        verify(strategy).validateAndAuthenticate(request);
        verify(userService).createUser(result);
        verify(userAuthorizationService).authorize(user);
    }

    @Test
    void registerThrowsExceptionWhenAuthenticationFails() {
        Verifiable request = mock(Verifiable.class);
        AuthenticationStrategy strategy = mock(AuthenticationStrategy.class);
        AuthenticationResult result = AuthenticationResult.failed("Authentication failed");

        when(request.getType()).thenReturn(AuthenticationType.OTP);
        when(strategyProvider.getStrategy(AuthenticationType.OTP)).thenReturn(strategy);
        when(strategy.validateAndAuthenticate(request)).thenReturn(result);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> authenticationService.register(request));

        assertEquals("Authentication failed", exception.getMessage());
        verify(strategyProvider).getStrategy(AuthenticationType.OTP);
        verify(strategy).validateAndAuthenticate(request);
        verifyNoInteractions(userService, userAuthorizationService);
    }

    @Test
    void loginSuccessfullyLogsInUser() {
        Verifiable request = mock(Verifiable.class);
        AuthenticationStrategy strategy = mock(AuthenticationStrategy.class);
        AuthenticationResult result = AuthenticationResult.builder()
                .email("john.doe@example.com")
                .status(AuthenticationStatus.SUCCESS)
                .build();
        User user = new User();
        user.setIsActive(true);

        when(request.getType()).thenReturn(AuthenticationType.OTP);
        when(strategyProvider.getStrategy(AuthenticationType.OTP)).thenReturn(strategy);
        when(strategy.validateAndAuthenticate(request)).thenReturn(result);
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(userAuthorizationService.authorize(user)).thenReturn("mockToken");

        String token = authenticationService.login(request);

        assertEquals("mockToken", token);
        verify(strategyProvider).getStrategy(AuthenticationType.OTP);
        verify(strategy).validateAndAuthenticate(request);
        verify(userService).findByEmail("john.doe@example.com");
        verify(userAuthorizationService).authorize(user);
    }

    @Test
    void loginThrowsExceptionWhenUserDoesNotExist() {
        Verifiable request = mock(Verifiable.class);
        AuthenticationStrategy strategy = mock(AuthenticationStrategy.class);
        AuthenticationResult result = AuthenticationResult.builder()
                .email("john.doe@example.com")
                .status(AuthenticationStatus.SUCCESS)
                .build();

        when(request.getType()).thenReturn(AuthenticationType.OTP);
        when(strategyProvider.getStrategy(AuthenticationType.OTP)).thenReturn(strategy);
        when(strategy.validateAndAuthenticate(request)).thenReturn(result);
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> authenticationService.login(request));

        assertEquals("User does not exist", exception.getMessage());
        verify(strategyProvider).getStrategy(AuthenticationType.OTP);
        verify(strategy).validateAndAuthenticate(request);
        verify(userService).findByEmail("john.doe@example.com");
        verifyNoInteractions(userAuthorizationService);
    }

    @Test
    void loginThrowsExceptionWhenUserIsInactive() {
        Verifiable request = mock(Verifiable.class);
        AuthenticationStrategy strategy = mock(AuthenticationStrategy.class);
        AuthenticationResult result = AuthenticationResult.builder()
                .email("john.doe@example.com")
                .status(AuthenticationStatus.SUCCESS)
                .build();
        User user = new User();
        user.setIsActive(false);

        when(request.getType()).thenReturn(AuthenticationType.OTP);
        when(strategyProvider.getStrategy(AuthenticationType.OTP)).thenReturn(strategy);
        when(strategy.validateAndAuthenticate(request)).thenReturn(result);
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> authenticationService.login(request));

        assertEquals("Forbidden user", exception.getMessage());
        verify(strategyProvider).getStrategy(AuthenticationType.OTP);
        verify(strategy).validateAndAuthenticate(request);
        verify(userService).findByEmail("john.doe@example.com");
        verifyNoInteractions(userAuthorizationService);
    }
}