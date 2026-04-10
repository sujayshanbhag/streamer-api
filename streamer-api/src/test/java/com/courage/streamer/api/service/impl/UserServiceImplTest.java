package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.dto.UserPageDto;
import com.courage.streamer.api.exception.AuthenticationException;
import com.courage.streamer.common.dto.VideoDto;
import com.courage.streamer.common.entity.User;
import com.courage.streamer.common.enums.VideoStatus;
import com.courage.streamer.common.repository.UserRepository;
import com.courage.streamer.common.repository.VideoRepository;
import com.courage.streamer.api.service.UserService;
import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserService userService;
    private UserRepository userRepository;
    private VideoRepository videoRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        videoRepository = mock(VideoRepository.class);
        userService = new UserServiceImpl(userRepository, videoRepository);
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

    @Test
    void getUserDetailsWithLiveVideosReturnsSinglePageWhenResultsWithinSize() {
        User user = new User();
        user.setId(42L);
        user.setName("Alice");

        VideoDto video = new VideoDto();
        video.setCreatedAt(Instant.parse("2024-06-01T10:00:00Z"));

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(videoRepository.findLiveByUserIdWithCursor(eq(42L), isNull(), any(Pageable.class)))
                .thenReturn(List.of(video));
        when(videoRepository.countByCreatedByAndStatus(42L, VideoStatus.LIVE)).thenReturn(1L);

        UserPageDto result = userService.getUserDetailsWithLiveVideos(42L, null, 10);

        assertEquals(user, result.getUser());
        assertEquals(1L, result.getTotalVideos());
        assertEquals(1, result.getVideos().getVideos().size());
        assertFalse(result.getVideos().isHasNextPage());
        assertNull(result.getVideos().getNextCursor());
    }

    @Test
    void getUserDetailsWithLiveVideosReturnsHasNextPageWhenMoreResultsThanSize() {
        User user = new User();
        user.setId(42L);

        Instant t1 = Instant.parse("2024-06-01T10:00:00Z");
        Instant t2 = Instant.parse("2024-06-01T09:00:00Z");
        VideoDto v1 = new VideoDto();
        v1.setCreatedAt(t1);
        VideoDto v2 = new VideoDto();
        v2.setCreatedAt(t2);

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(videoRepository.findLiveByUserIdWithCursor(eq(42L), isNull(), any(Pageable.class)))
                .thenReturn(List.of(v1, v2));
        when(videoRepository.countByCreatedByAndStatus(42L, VideoStatus.LIVE)).thenReturn(5L);

        UserPageDto result = userService.getUserDetailsWithLiveVideos(42L, null, 1);

        assertEquals(1, result.getVideos().getVideos().size());
        assertTrue(result.getVideos().isHasNextPage());
        assertEquals(t1.toString(), result.getVideos().getNextCursor());
        assertEquals(5L, result.getTotalVideos());
    }

    @Test
    void getUserDetailsWithLiveVideosParsesCursorString() {
        User user = new User();
        user.setId(42L);
        String cursorStr = "2024-03-15T08:00:00Z";
        Instant cursor = Instant.parse(cursorStr);

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(videoRepository.findLiveByUserIdWithCursor(eq(42L), eq(cursor), any(Pageable.class)))
                .thenReturn(List.of());
        when(videoRepository.countByCreatedByAndStatus(42L, VideoStatus.LIVE)).thenReturn(0L);

        UserPageDto result = userService.getUserDetailsWithLiveVideos(42L, cursorStr, 10);

        assertNotNull(result);
        verify(videoRepository).findLiveByUserIdWithCursor(eq(42L), eq(cursor), any(Pageable.class));
    }

    @Test
    void getUserDetailsWithLiveVideosRequestsPageSizePlusOne() {
        User user = new User();
        user.setId(42L);

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(videoRepository.findLiveByUserIdWithCursor(eq(42L), isNull(), argThat(p -> p.getPageSize() == 6)))
                .thenReturn(List.of());
        when(videoRepository.countByCreatedByAndStatus(42L, VideoStatus.LIVE)).thenReturn(0L);

        userService.getUserDetailsWithLiveVideos(42L, null, 5);

        verify(videoRepository).findLiveByUserIdWithCursor(eq(42L), isNull(), argThat(p -> p.getPageSize() == 6));
    }

    @Test
    void getUserDetailsWithLiveVideosThrowsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.getUserDetailsWithLiveVideos(99L, null, 10));

        verify(videoRepository, never()).findLiveByUserIdWithCursor(anyLong(), any(), any(Pageable.class));
    }

    @Test
    void getUserDetailsWithLiveVideosCountsOnlyLiveVideos() {
        User user = new User();
        user.setId(42L);

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(videoRepository.findLiveByUserIdWithCursor(eq(42L), isNull(), any(Pageable.class)))
                .thenReturn(List.of());
        when(videoRepository.countByCreatedByAndStatus(42L, VideoStatus.LIVE)).thenReturn(7L);

        UserPageDto result = userService.getUserDetailsWithLiveVideos(42L, null, 10);

        assertEquals(7L, result.getTotalVideos());
        verify(videoRepository).countByCreatedByAndStatus(42L, VideoStatus.LIVE);
    }
}