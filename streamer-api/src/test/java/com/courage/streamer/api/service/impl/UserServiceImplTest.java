package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.exception.AuthenticationException;
import com.courage.streamer.api.model.entity.User;
import com.courage.streamer.api.repository.UserRepository;
import com.courage.streamer.api.service.UserService;
import com.courage.streamer.api.service.impl.UserServiceImpl;
import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void createUserSuccessfullyCreatesNewUser() {
        AuthenticationResult authResult = AuthenticationResult.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .build();

        User savedUser = new User();
        savedUser.setName("John Doe");
        savedUser.setEmail("john.doe@example.com");
        savedUser.setPhoneNumber("1234567890");
        savedUser.setIsActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User createdUser = userService.createUser(authResult);

        assertNotNull(createdUser);
        assertEquals("John Doe", createdUser.getName());
        assertEquals("john.doe@example.com", createdUser.getEmail());
        assertEquals("1234567890", createdUser.getPhoneNumber());
        assertTrue(createdUser.getIsActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserThrowsExceptionWhenEmailAlreadyExists() {
        AuthenticationResult authResult = AuthenticationResult.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .build();

        when(userRepository.save(any(User.class))).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate email"));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> userService.createUser(authResult));

        assertEquals("User with this email already exists", exception.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findByEmailReturnsUserWhenExists() {
        String email = "john.doe@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByEmail(email);

        assertTrue(foundUser.isPresent());
        assertEquals(email, foundUser.get().getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmailReturnsEmptyWhenUserDoesNotExist() {
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.findByEmail(email);

        assertFalse(foundUser.isPresent());
        verify(userRepository).findByEmail(email);
    }
}