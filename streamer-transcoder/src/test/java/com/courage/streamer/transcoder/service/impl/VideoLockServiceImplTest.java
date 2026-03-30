package com.courage.streamer.transcoder.service.impl;

import com.courage.streamer.common.entity.Video;
import com.courage.streamer.common.enums.VideoStatus;
import com.courage.streamer.common.repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoLockServiceImplTest {

    private VideoRepository videoRepository;
    private VideoLockServiceImpl videoLockService;

    @BeforeEach
    void setUp() {
        videoRepository = mock(VideoRepository.class);
        videoLockService = new VideoLockServiceImpl(videoRepository);
    }

    @Test
    void acquireAndUpdateVideoTransitionsUploadedToProcessing() {
        UUID stagingId = UUID.randomUUID();
        Video video = new Video();
        video.setId(stagingId);
        video.setStatus(VideoStatus.UPLOADED);

        when(videoRepository.findByIdForUpdate(stagingId)).thenReturn(Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Video> result = videoLockService.acquireAndUpdateVideo(stagingId);

        assertTrue(result.isPresent());
        assertEquals(VideoStatus.PROCESSING, result.get().getStatus());
        verify(videoRepository).save(video);
    }

    @Test
    void acquireAndUpdateVideoTransitionsRetryToProcessing() {
        UUID stagingId = UUID.randomUUID();
        Video video = new Video();
        video.setId(stagingId);
        video.setStatus(VideoStatus.RETRY);

        when(videoRepository.findByIdForUpdate(stagingId)).thenReturn(Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Video> result = videoLockService.acquireAndUpdateVideo(stagingId);

        assertTrue(result.isPresent());
        assertEquals(VideoStatus.PROCESSING, result.get().getStatus());
    }

    @Test
    void acquireAndUpdateVideoReturnsNullWhenAlreadyProcessing() {
        UUID stagingId = UUID.randomUUID();
        Video video = new Video();
        video.setId(stagingId);
        video.setStatus(VideoStatus.PROCESSING);

        when(videoRepository.findByIdForUpdate(stagingId)).thenReturn(Optional.of(video));

        Optional<Video> result = videoLockService.acquireAndUpdateVideo(stagingId);

        assertNull(result);
        verify(videoRepository, never()).save(any());
    }

    @Test
    void acquireAndUpdateVideoReturnsNullWhenAlreadyLive() {
        UUID stagingId = UUID.randomUUID();
        Video video = new Video();
        video.setId(stagingId);
        video.setStatus(VideoStatus.LIVE);

        when(videoRepository.findByIdForUpdate(stagingId)).thenReturn(Optional.of(video));

        Optional<Video> result = videoLockService.acquireAndUpdateVideo(stagingId);

        assertNull(result);
        verify(videoRepository, never()).save(any());
    }

    @Test
    void acquireAndUpdateVideoThrowsWhenVideoNotFound() {
        UUID stagingId = UUID.randomUUID();
        when(videoRepository.findByIdForUpdate(stagingId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> videoLockService.acquireAndUpdateVideo(stagingId));
    }
}
