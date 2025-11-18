package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import com.courage.streamer.api.exception.AuthenticationException;
import com.courage.streamer.api.model.entity.User;
import com.courage.streamer.api.model.entity.UserPermission;
import com.courage.streamer.api.model.enums.PermissionType;
import com.courage.streamer.api.repository.UserRepository;
import com.courage.streamer.api.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
            throw new AuthenticationException("User with this email already exists");
        }
    }


    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
