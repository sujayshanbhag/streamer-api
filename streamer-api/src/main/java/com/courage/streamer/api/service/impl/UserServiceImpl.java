package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.dto.UserPageDto;
import com.courage.streamer.api.dto.VideoPageResponse;
import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import com.courage.streamer.api.exception.AuthenticationException;
import com.courage.streamer.common.dto.VideoDto;
import com.courage.streamer.common.entity.User;
import com.courage.streamer.common.entity.UserPermission;
import com.courage.streamer.common.entity.Video;
import com.courage.streamer.common.enums.VideoStatus;
import com.courage.streamer.common.exception.enums.PermissionType;
import com.courage.streamer.common.repository.UserRepository;
import com.courage.streamer.api.service.UserService;
import com.courage.streamer.common.repository.VideoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    public UserServiceImpl(UserRepository userRepository, VideoRepository videoRepository) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }

    @Override
    public User createUser(AuthenticationResult authResult) {
        try {
            User newUser = new User();
            newUser.setName(authResult.getName());
            newUser.setEmail(authResult.getEmail());
            newUser.setPhoneNumber(authResult.getPhoneNumber());
            var defaultPermission = new UserPermission();
            defaultPermission.setType(PermissionType.VIEWER);
            newUser.getPermissions().add(defaultPermission);
            newUser.setIsActive(true);
            return userRepository.save(newUser);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Duplicate user registration attempt for email: {}", authResult.getEmail(), e);
            throw new AuthenticationException("User with this email already exists");
        }
    }


    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new AuthenticationException("User not found with id: " + id));
    }

    @Override
    public UserPageDto getUserDetailsWithLiveVideos(Long userId, String cursorStr, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Instant cursor = cursorStr != null ? Instant.parse(cursorStr) : null;
        List<VideoDto> videos = videoRepository.findLiveByUserIdWithCursor(userId, cursor, Pageable.ofSize(size + 1));
        long count = videoRepository.countByCreatedByAndStatus(userId, VideoStatus.LIVE);

        boolean hasNextPage = videos.size() > size;
        List<VideoDto> pageItems = hasNextPage ? videos.subList(0, size) : videos;

        String nextCursor = hasNextPage
                ? pageItems.get(pageItems.size() - 1).getCreatedAt().toString()
                : null;

        VideoPageResponse videoPageResponse = new VideoPageResponse(pageItems, nextCursor, hasNextPage);

        return new UserPageDto(user, count, videoPageResponse);
    }
}
