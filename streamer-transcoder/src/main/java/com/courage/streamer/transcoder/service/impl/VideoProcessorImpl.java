package com.courage.streamer.transcoder.service.impl;

import com.courage.streamer.common.entity.Video;
import com.courage.streamer.common.repository.VideoRepository;
import com.courage.streamer.transcoder.model.TranscoderMessage;
import com.courage.streamer.transcoder.service.S3Service;
import com.courage.streamer.transcoder.service.TranscoderService;
import com.courage.streamer.transcoder.service.VideoProcessor;
import com.courage.streamer.common.enums.VideoStatus;
import com.courage.streamer.transcoder.service.VideoLockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoProcessorImpl implements VideoProcessor {

    private final S3Service s3Service;
    private final  TranscoderService transcoderService;
    private final VideoRepository videoRepository;
    private final VideoLockService videoLockService;
    private final ObjectMapper objectMapper;

    @Value("${sqs.transcoder.max-retry}")
    private Integer maxRetries;

    public VideoProcessorImpl(S3Service s3Service, TranscoderService transcoderService, VideoRepository videoRepository, VideoLockService videoLockService, ObjectMapper objectMapper) {
        this.s3Service = s3Service;
        this.transcoderService = transcoderService;
        this.videoRepository = videoRepository;
        this.videoLockService = videoLockService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void process(String message, int receiveCount) throws IOException {
        TranscoderMessage msg = objectMapper.readValue(message, TranscoderMessage.class);

        UUID stagingId = msg.getStagingId();
        String key = msg.getS3Key();
        String fileName = key.substring(key.lastIndexOf("/") + 1);
        String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf("."));

        Optional<Video> videoOptional = videoLockService.acquireAndUpdateVideo(stagingId);
        if(videoOptional.isEmpty()) {
            System.out.println("Duplicate event, skipping. stagingId: " + stagingId);
            return;
        }
        Video video = videoOptional.get();
        
        String tempDir = System.getProperty("java.io.tmpdir");

        String inputPath = tempDir + File.separator + "input" + File.separator + fileName;
        String outputPath = tempDir + File.separator + "output" + File.separator + fileNameWithoutExt;

        File downloadedFile = new File(inputPath);
        File outputPathFile = new File(outputPath);

        try {
            downloadedFile.getParentFile().mkdirs();
            outputPathFile.mkdirs();

            // 1. Download
            s3Service.downloadFile(key, inputPath);

            // 2. Transcode
            transcoderService.transcode(inputPath, outputPath);

            // 3. Upload
            String s3OutputPrefix = "processed/" + stagingId;
            uploadDirectory(outputPathFile, outputPath, s3OutputPrefix);

            System.out.println("Transcoding and upload completed for: " + key);

            video.setStatus(VideoStatus.LIVE);
            video.setKey360p(s3OutputPrefix + "/360p/index.m3u8");
            video.setKey720p(s3OutputPrefix + "/720p/index.m3u8");
            video.setKey1080p(s3OutputPrefix + "/1080p/index.m3u8");
            videoRepository.save(video);

        } catch (Exception e) {
            if (receiveCount >= maxRetries) {
                video.setStatus(VideoStatus.FAILED);
                System.out.println("Max retries reached, marking FAILED: " + stagingId);
            } else {
                video.setStatus(VideoStatus.RETRY);
                System.out.println("Attempt " + receiveCount + " failed, marking RETRY: " + stagingId);
            }
            videoRepository.save(video);
            throw new RuntimeException("Transcoding failed for stagingId: " + stagingId, e);
        } finally {
            deleteDirectory(outputPathFile);
            downloadedFile.delete();
        }
    }


    private void uploadDirectory(File directory, String localBaseDir, String s3Prefix) {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                uploadDirectory(file, localBaseDir, s3Prefix);
            } else {
                // preserve relative path: 360p/index.m3u8, 360p/segment_000.ts etc
                String relativePath = file.getAbsolutePath()
                        .substring(localBaseDir.length())
                        .replace("\\", "/")  // Windows path fix
                        .replaceFirst("^/", "");
                String s3Key = s3Prefix + "/" + relativePath;
                s3Service.uploadFile(file, s3Key);
            }
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        Path dirPath = directory.toPath();
        if (!Files.exists(dirPath)) return;

        Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                boolean deleted = file.toFile().delete();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                boolean deleted = dir.toFile().delete();
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
