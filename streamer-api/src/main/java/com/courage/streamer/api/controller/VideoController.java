package com.courage.streamer.api.controller;

import com.courage.streamer.api.dto.VideoRequestDto;
import com.courage.streamer.api.dto.VideoResponseDto;
import com.courage.streamer.api.service.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/video")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<VideoResponseDto> initiateUpload(@RequestBody VideoRequestDto request) {
        VideoResponseDto response = videoService.initiateUpload(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

