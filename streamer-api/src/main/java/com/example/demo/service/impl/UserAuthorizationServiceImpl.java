package com.example.demo.service.impl;

import com.example.demo.model.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;
import com.example.demo.service.UserAuthorizationService;
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
    public boolean validate(Long userId, String version) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            return user.getVersion().equals(version);
        }
        return false;
    }
}

