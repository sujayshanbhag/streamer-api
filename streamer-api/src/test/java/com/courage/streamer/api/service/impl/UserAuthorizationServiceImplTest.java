package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.model.entity.User;
import com.courage.streamer.api.model.entity.UserPermission;
import com.courage.streamer.api.model.enums.PermissionType;
import com.courage.streamer.api.repository.UserRepository;
import com.courage.streamer.api.service.JwtService;
import com.courage.streamer.api.service.impl.UserAuthorizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAuthorizationServiceImplTest {

    private UserAuthorizationServiceImpl userAuthorizationService;
    private JwtService jwtService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        userAuthorizationService = new UserAuthorizationServiceImpl(jwtService, userRepository);
    }

    @Test
    void authorizeGeneratesTokenWithPermissions() {
        User user = new User();
        UserPermission userPermission = new UserPermission();
        userPermission.setType(PermissionType.VIEWER);
        user.setId(1L);
        user.setVersion("1");
        user.setPermissions(List.of(userPermission));

        when(jwtService.generateToken(anyString(), anyList(), anyString())).thenReturn("mockToken");

        String token = userAuthorizationService.authorize(user);

        assertEquals("mockToken", token);
        verify(jwtService).generateToken("1", List.of("VIEWER"), "1");
    }

    @Test
    void authorizeGeneratesTokenWithoutPermissions() {
        User user = new User();
        user.setId(1L);
        user.setVersion("1");
        user.setPermissions(Collections.emptyList());

        when(jwtService.generateToken("1", Collections.emptyList(), "1")).thenReturn("mockToken");

        String token = userAuthorizationService.authorize(user);

        assertEquals("mockToken", token);
        verify(jwtService).generateToken("1", Collections.emptyList(), "1");
    }

    @Test
    void validateReturnsTrueWhenUserExistsAndVersionMatches() {
        User user = new User();
        user.setId(1L);
        user.setVersion("1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean isValid = userAuthorizationService.validate(1L, "1");

        assertTrue(isValid);
        verify(userRepository).findById(1L);
    }

    @Test
    void validateReturnsFalseWhenUserExistsAndVersionDoesNotMatch() {
        User user = new User();
        user.setId(1L);
        user.setVersion("1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean isValid = userAuthorizationService.validate(1L, "2");

        assertFalse(isValid);
        verify(userRepository).findById(1L);
    }

    @Test
    void validateReturnsFalseWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean isValid = userAuthorizationService.validate(1L, "1");

        assertFalse(isValid);
        verify(userRepository).findById(1L);
    }
}