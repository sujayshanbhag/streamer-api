package com.courage.streamer.transcoder.service;

import com.courage.streamer.common.entity.Video;

import java.util.Optional;
import java.util.UUID;

public interface VideoLockService {
    Optional<Video> acquireAndUpdateVideo(UUID stagingId);
}
