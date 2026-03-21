package com.courage.streamer.transcoder.service.impl;

import com.courage.streamer.common.entity.Video;
import com.courage.streamer.common.enums.VideoStatus;
import com.courage.streamer.common.repository.VideoRepository;
import com.courage.streamer.transcoder.service.VideoLockService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoLockServiceImpl implements VideoLockService {

    private final VideoRepository videoRepository;

    @Transactional
    public Optional<Video> acquireAndUpdateVideo(UUID stagingId) {
        Video video = videoRepository.findByIdForUpdate(stagingId)
                .orElseThrow(() -> new EntityNotFoundException("Video not found for stagingId: " + stagingId));

        VideoStatus status = video.getStatus();
        if (status != VideoStatus.UPLOADED && status != VideoStatus.RETRY) {
            System.out.println("Duplicate event: " + stagingId + ", video status: " + status);
            return null;
        }

        video.setStatus(VideoStatus.PROCESSING);
        return Optional.of(videoRepository.save(video));
    }
}

