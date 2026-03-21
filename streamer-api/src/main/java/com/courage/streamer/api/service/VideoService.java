package com.courage.streamer.api.service;

import com.courage.streamer.api.dto.VideoRequestDto;
import com.courage.streamer.api.dto.VideoResponseDto;

public interface VideoService {
    VideoResponseDto initiateUpload(VideoRequestDto request);
}
