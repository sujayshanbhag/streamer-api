package com.courage.streamer.api.service;

import java.util.UUID;

public interface S3Service {
    String generatePresignedUrl(UUID uuid, String fileName, String contentType);
    String generateVideoUploadUrl(UUID uuid, String fileName);
    String generateImageUploadUrl(UUID uuid, String fileName);

    String generateKey(String folder, UUID uuid, String fileName);
}
