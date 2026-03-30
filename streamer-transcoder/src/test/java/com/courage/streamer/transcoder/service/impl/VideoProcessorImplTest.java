package com.courage.streamer.transcoder.service.impl;

import com.courage.streamer.common.entity.Video;
import com.courage.streamer.common.enums.VideoStatus;
import com.courage.streamer.common.repository.VideoRepository;
import com.courage.streamer.transcoder.model.TranscoderMessage;
import com.courage.streamer.transcoder.service.S3Service;
import com.courage.streamer.transcoder.service.TranscoderService;
import com.courage.streamer.transcoder.service.VideoLockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoProcessorImplTest {

    private S3Service s3Service;
    private TranscoderService transcoderService;
    private VideoRepository videoRepository;
    private VideoLockService videoLockService;
    private VideoProcessorImpl videoProcessor;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() {
        s3Service = mock(S3Service.class);
        transcoderService = mock(TranscoderService.class);
        videoRepository = mock(VideoRepository.class);
        videoLockService = mock(VideoLockService.class);
        videoProcessor = new VideoProcessorImpl(s3Service, transcoderService, videoRepository, videoLockService, OBJECT_MAPPER);
        ReflectionTestUtils.setField(videoProcessor, "maxRetries", 3);
    }

    private String buildMessage(UUID stagingId, String key) throws Exception {
        TranscoderMessage msg = new TranscoderMessage(1L, stagingId, "Test", "Desc", key);
        return OBJECT_MAPPER.writeValueAsString(msg);
    }

    @Test
    void processSkipsWhenLockNotAcquired() throws Exception {
        UUID stagingId = UUID.randomUUID();
        String key = "uploads/video_" + stagingId + ".mp4";

        when(videoLockService.acquireAndUpdateVideo(stagingId)).thenReturn(Optional.empty());

        videoProcessor.process(buildMessage(stagingId, key), 1);

        verifyNoInteractions(s3Service, transcoderService);
        verify(videoRepository, never()).save(any());
    }

    @Test
    void processSuccessfullyTranscodesAndSetsStatusLive() throws Exception {
        UUID stagingId = UUID.randomUUID();
        String key = "uploads/video_" + stagingId + ".mp4";
        Video video = new Video();
        video.setId(stagingId);
        video.setStatus(VideoStatus.PROCESSING);

        when(videoLockService.acquireAndUpdateVideo(stagingId)).thenReturn(Optional.of(video));
        doNothing().when(s3Service).downloadFile(anyString(), anyString());
        doNothing().when(transcoderService).transcode(anyString(), anyString());
        doNothing().when(s3Service).uploadFile(any(), anyString());
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        videoProcessor.process(buildMessage(stagingId, key), 1);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(captor.capture());
        assertEquals(VideoStatus.LIVE, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getKey360p());
        assertNotNull(captor.getValue().getKey720p());
        assertNotNull(captor.getValue().getKey1080p());
    }

    @Test
    void processMarksRetryWhenTranscodeFailsBeforeMaxRetries() throws Exception {
        UUID stagingId = UUID.randomUUID();
        String key = "uploads/video_" + stagingId + ".mp4";
        Video video = new Video();
        video.setId(stagingId);
        video.setStatus(VideoStatus.PROCESSING);

        when(videoLockService.acquireAndUpdateVideo(stagingId)).thenReturn(Optional.of(video));
        doThrow(new IOException("ffmpeg error")).when(transcoderService).transcode(anyString(), anyString());
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(RuntimeException.class,
                () -> videoProcessor.process(buildMessage(stagingId, key), 1));

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(captor.capture());
        assertEquals(VideoStatus.RETRY, captor.getValue().getStatus());
    }

    @Test
    void processMarksFailedWhenTranscodeFailsAtMaxRetries() throws Exception {
        UUID stagingId = UUID.randomUUID();
        String key = "uploads/video_" + stagingId + ".mp4";
        Video video = new Video();
        video.setId(stagingId);
        video.setStatus(VideoStatus.PROCESSING);

        when(videoLockService.acquireAndUpdateVideo(stagingId)).thenReturn(Optional.of(video));
        doThrow(new IOException("ffmpeg error")).when(transcoderService).transcode(anyString(), anyString());
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(RuntimeException.class,
                () -> videoProcessor.process(buildMessage(stagingId, key), 3));

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(captor.capture());
        assertEquals(VideoStatus.FAILED, captor.getValue().getStatus());
    }

    @Test
    void processMarksFailedWhenReceiveCountExceedsMaxRetries() throws Exception {
        UUID stagingId = UUID.randomUUID();
        String key = "uploads/video_" + stagingId + ".mp4";
        Video video = new Video();
        video.setId(stagingId);
        video.setStatus(VideoStatus.PROCESSING);

        when(videoLockService.acquireAndUpdateVideo(stagingId)).thenReturn(Optional.of(video));
        doThrow(new IOException("error")).when(transcoderService).transcode(anyString(), anyString());
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(RuntimeException.class,
                () -> videoProcessor.process(buildMessage(stagingId, key), 5));

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(captor.capture());
        assertEquals(VideoStatus.FAILED, captor.getValue().getStatus());
    }
}
