package com.courage.streamer.api.service;

import com.courage.streamer.api.dto.AccountPageDto;
import com.courage.streamer.api.dto.UploadRequestDto;
import com.courage.streamer.api.dto.UploadResponseDto;
import com.courage.streamer.api.dto.VideoPageResponse;
import com.courage.streamer.common.dto.VideoDto;

import java.util.UUID;


public interface VideoService {
    UploadResponseDto initiateUpload(UploadRequestDto request);
    VideoPageResponse getLiveVideos(String keyword, String cursor, int size);
    AccountPageDto getUserVideos(String key, Long userId, String cursor, int size);
    VideoDto findVideoById(UUID videoId);
}
