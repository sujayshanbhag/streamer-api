package com.courage.streamer.transcoder.service.impl;

import com.courage.streamer.common.entity.Video;
import com.courage.streamer.common.entity.VideoStaging;
import com.courage.streamer.common.enums.VideoStatus;
import com.courage.streamer.common.repository.VideoRepository;
import com.courage.streamer.common.repository.VideoStagingRepository;
import com.courage.streamer.transcoder.model.TranscoderMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UploadEventServiceImplTest {

    private VideoRepository videoRepository;
    private VideoStagingRepository videoStagingRepository;
    private SqsTemplate sqsTemplate;
    private UploadEventServiceImpl uploadEventService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123456789/transcoder-queue";

    @BeforeEach
    void setUp() {
        videoRepository = mock(VideoRepository.class);
        videoStagingRepository = mock(VideoStagingRepository.class);
        sqsTemplate = mock(SqsTemplate.class);
        uploadEventService = new UploadEventServiceImpl(videoRepository, videoStagingRepository, sqsTemplate, OBJECT_MAPPER);
        ReflectionTestUtils.setField(uploadEventService, "transcoderQueueUrl", QUEUE_URL);
    }

    private String buildS3Event(String key) {
        return """
                {
                  "Records": [{
                    "eventTime": "2024-01-01T00:00:00.000Z",
                    "s3": {
                      "object": { "key": "%s" }
                    }
                  }]
                }
                """.formatted(key);
    }

    @Test
    void handleUploadSavesVideoAndSendsTranscoderMessage() throws IOException {
        UUID stagingId = UUID.randomUUID();
        String key = "uploads/video_" + stagingId + ".mp4";

        VideoStaging staging = new VideoStaging();
        staging.setId(stagingId);
        staging.setCreatedBy(1L);
        staging.setTitle("Test Video");
        staging.setDescription("A description");
        staging.setThumbnailKey(null);

        when(videoRepository.existsById(stagingId)).thenReturn(false);
        when(videoStagingRepository.findById(stagingId)).thenReturn(Optional.of(staging));
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        uploadEventService.handleUpload(buildS3Event(key));

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(videoCaptor.capture());
        Video saved = videoCaptor.getValue();
        assertEquals(stagingId, saved.getId());
        assertEquals(VideoStatus.UPLOADED, saved.getStatus());
        assertEquals("Test Video", saved.getTitle());

        verify(sqsTemplate).send(eq(QUEUE_URL), anyString());
    }

    @Test
    void handleUploadSkipsWhenVideoAlreadyExists() throws IOException {
        UUID stagingId = UUID.randomUUID();
        String key = "uploads/video_" + stagingId + ".mp4";

        when(videoRepository.existsById(stagingId)).thenReturn(true);

        uploadEventService.handleUpload(buildS3Event(key));

        verify(videoRepository, never()).save(any());
        verify(sqsTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void handleUploadDoesNothingWhenRecordsIsMissing() throws IOException {
        String message = "{}";

        uploadEventService.handleUpload(message);

        verify(videoRepository, never()).save(any());
        verify(sqsTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void handleUploadDoesNothingWhenRecordsIsEmpty() throws IOException {
        String message = "{\"Records\": []}";

        uploadEventService.handleUpload(message);

        verify(videoRepository, never()).save(any());
        verify(sqsTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void handleUploadThrowsWhenStagingNotFound() {
        UUID stagingId = UUID.randomUUID();
        String key = "uploads/video_" + stagingId + ".mp4";

        when(videoRepository.existsById(stagingId)).thenReturn(false);
        when(videoStagingRepository.findById(stagingId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> uploadEventService.handleUpload(buildS3Event(key)));
    }

    @Test
    void handleUploadDecodesUrlEncodedKey() throws IOException {
        UUID stagingId = UUID.randomUUID();
        // URL-encoded key: spaces become +, '/'=>'%2F'
        String encodedKey = "uploads/my+video_" + stagingId + ".mp4";
        String decodedKey = "uploads/my video_" + stagingId + ".mp4";

        VideoStaging staging = new VideoStaging();
        staging.setId(stagingId);
        staging.setCreatedBy(1L);
        staging.setTitle("My Video");
        staging.setDescription(null);
        staging.setThumbnailKey(null);

        when(videoRepository.existsById(stagingId)).thenReturn(false);
        when(videoStagingRepository.findById(stagingId)).thenReturn(Optional.of(staging));
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        uploadEventService.handleUpload(buildS3Event(encodedKey));

        // Verify SQS message contains the decoded key
        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        verify(sqsTemplate).send(eq(QUEUE_URL), msgCaptor.capture());
        assertTrue(msgCaptor.getValue().contains(decodedKey));
    }
}
