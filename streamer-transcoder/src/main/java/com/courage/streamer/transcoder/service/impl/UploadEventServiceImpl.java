package com.courage.streamer.transcoder.service.impl;

import com.courage.streamer.common.entity.Video;
import com.courage.streamer.common.entity.VideoStaging;
import com.courage.streamer.common.enums.VideoStatus;
import com.courage.streamer.common.repository.VideoRepository;
import com.courage.streamer.common.repository.VideoStagingRepository;
import com.courage.streamer.transcoder.model.TranscoderMessage;
import com.courage.streamer.transcoder.service.UploadEventService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Service
public class UploadEventServiceImpl implements UploadEventService {

    private final VideoRepository videoRepository;
    private final VideoStagingRepository videoStagingRepository;
    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${sqs.transcoder.queue-url}")
    private String transcoderQueueUrl;

    public UploadEventServiceImpl(VideoRepository videoRepository, VideoStagingRepository videoStagingRepository, SqsTemplate sqsTemplate, ObjectMapper objectMapper) {
        this.videoRepository = videoRepository;
        this.videoStagingRepository = videoStagingRepository;
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void handleUpload(String message) throws IOException {
        JsonNode root = objectMapper.readTree(message);
        JsonNode records = root.path("Records");
        if (records.isMissingNode() || records.isEmpty()) return;

        String rawKey = records.path(0).path("s3").path("object").path("key").asText();
        String key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);

        String fileName = key.substring(key.lastIndexOf("/") + 1);
        UUID stagingId = UUID.fromString(fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf(".")));

        // Guard
        if (videoRepository.existsById(stagingId)) {
            System.out.println("Duplicate upload event, skipping: " + stagingId);
            return;
        }

        // Fetch staging metadata
        VideoStaging staging = videoStagingRepository.findById(stagingId)
                .orElseThrow(() -> new EntityNotFoundException("Staging not found: " + stagingId));

        String eventTime = records.path(0).path("eventTime").asText();
        Instant createdAt = Instant.parse(eventTime);
        Video video = new Video();
        video.setId(stagingId);
        video.setUserId(staging.getUserId());
        video.setTitle(staging.getTitle());
        video.setDescription(staging.getDescription());
        video.setOriginalFileName(staging.getOriginalFileName());
        video.setStatus(VideoStatus.UPLOADED);
        video.setCreatedAt(createdAt);
        videoRepository.save(video);
        System.out.println("Video metadata saved: " + stagingId);
        TranscoderMessage transcoderMessage = new TranscoderMessage(staging.getUserId(), staging.getId(), staging.getTitle(), staging.getDescription(),  key, staging.getOriginalFileName());
        sqsTemplate.send(transcoderQueueUrl, objectMapper.writeValueAsString(transcoderMessage));
        System.out.println("Transcoder message sent to SQS: " + stagingId);
    }
}

