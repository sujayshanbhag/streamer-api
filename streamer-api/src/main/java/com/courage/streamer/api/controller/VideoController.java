package com.courage.streamer.api.controller;


import com.courage.streamer.api.dto.VideoPageResponse;
import com.courage.streamer.api.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }



    @GetMapping
    public ResponseEntity<VideoPageResponse> getLiveVideos(
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        return ResponseEntity.ok(videoService.getLiveVideos(cursor, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<VideoPageResponse> getUserVideos(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        return ResponseEntity.ok(videoService.getUserVideos(userId, cursor, size));
    }
}

