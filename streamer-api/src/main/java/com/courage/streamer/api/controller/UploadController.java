package com.courage.streamer.api.controller;

import com.courage.streamer.api.dto.UploadRequestDto;
import com.courage.streamer.api.dto.UploadResponseDto;
import com.courage.streamer.api.service.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final VideoService videoService;

    public UploadController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping
    public ResponseEntity<UploadResponseDto> initiateUpload(@RequestBody UploadRequestDto request) {
        UploadResponseDto response = videoService.initiateUpload(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
