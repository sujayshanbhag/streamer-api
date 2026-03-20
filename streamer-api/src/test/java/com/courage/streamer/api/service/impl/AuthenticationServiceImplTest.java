package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.constant.AuthenticationStatus;
import com.courage.streamer.api.constant.AuthenticationType;
import com.courage.streamer.api.exception.AuthenticationException;
import com.courage.streamer.api.exception.ForbiddenException;
import com.courage.streamer.api.model.dto.TokenResponseDto;
import com.courage.streamer.api.service.JwtService;
import com.courage.streamer.api.service.UserService;
import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import com.courage.streamer.api.strategy.auth.AuthenticationStrategy;
import com.courage.streamer.api.strategy.auth.AuthenticationStrategyProvider;
import com.courage.streamer.api.strategy.auth.Verifiable;
import com.courage.streamer.common.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    private AuthenticationStrategyProvider strategyProvider;
    private UserService userService;
    private UserAuthorizationServiceImpl userAuthorizationService;
    private AuthenticationServiceImpl authenticationService;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        strategyProvider = mock(AuthenticationStrategyProvider.class);
        userService = mock(UserService.class);
        userAuthorizationService = Mockito.mock(UserAuthorizationServiceImpl.class);
        jwtService = mock(JwtService.class);
        authenticationService = new AuthenticationServiceImpl(strategyProvider, userService, jwtService, userAuthorizationService);
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
        TokenResponseDto mockTokens = TokenResponseDto.builder()
                        .accessToken("accessToken")
                        .refreshToken("refreshToken")
                        .build();

        when(request.getType()).thenReturn(AuthenticationType.OTP);
        when(strategyProvider.getStrategy(AuthenticationType.OTP)).thenReturn(strategy);
        when(strategy.validateAndAuthenticate(request)).thenReturn(result);
        when(userService.createUser(result)).thenReturn(user);
        when(userAuthorizationService.authorize(user)).thenReturn(mockTokens);

        TokenResponseDto token = authenticationService.register(request);

        assertEquals(mockTokens, token);
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
        TokenResponseDto mockTokens = TokenResponseDto.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        when(request.getType()).thenReturn(AuthenticationType.OTP);
        when(strategyProvider.getStrategy(AuthenticationType.OTP)).thenReturn(strategy);
        when(strategy.validateAndAuthenticate(request)).thenReturn(result);
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        when(userAuthorizationService.authorize(user)).thenReturn(mockTokens);


        TokenResponseDto token = authenticationService.login(request);

        assertEquals(mockTokens, token);
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