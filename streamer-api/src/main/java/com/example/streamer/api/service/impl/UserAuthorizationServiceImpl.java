package com.example.streamer.api.service.impl;

import com.example.streamer.api.model.entity.User;
import com.example.streamer.api.repository.UserRepository;
import com.example.streamer.api.service.JwtService;
import com.example.streamer.api.service.UserAuthorizationService;
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
    public String authorize(User user) {
        List<String> scopes = user.getPermissions().stream()
                                  .map((permission) -> permission.getType().toString())
                                  .toList();
        return jwtService.generateToken(user.getId().toString(), scopes, user.getVersion());
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
}

