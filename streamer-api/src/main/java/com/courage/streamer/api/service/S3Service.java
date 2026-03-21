package com.courage.streamer.api.service;

import java.util.UUID;

public interface S3Service {
    String generatePresignedUrl(UUID uuid, String fileName);
}
