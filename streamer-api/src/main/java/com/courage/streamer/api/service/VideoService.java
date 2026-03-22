package com.courage.streamer.api.service;

import com.courage.streamer.api.dto.UploadRequestDto;
import com.courage.streamer.api.dto.UploadResponseDto;
import com.courage.streamer.api.dto.VideoPageResponse;


public interface VideoService {
    UploadResponseDto initiateUpload(UploadRequestDto request);
    VideoPageResponse getLiveVideos(String cursor, int size);
    VideoPageResponse getUserVideos(Long userId, String cursor, int size);
}
