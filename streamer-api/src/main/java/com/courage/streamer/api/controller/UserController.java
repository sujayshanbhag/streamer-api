package com.courage.streamer.api.controller;

import com.courage.streamer.api.dto.UserPageDto;
import com.courage.streamer.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}/videos")
    public ResponseEntity<UserPageDto> getUserDetailsWithLiveVideos(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        UserPageDto response = userService.getUserDetailsWithLiveVideos(userId, cursor, size);
        return ResponseEntity.ok(response);
    }
}