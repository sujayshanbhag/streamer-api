package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.exception.CustomS3Exception;
import com.courage.streamer.api.service.S3Service;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private static final long EXPIRATION_MILI = 900_000; // 15 minutes

    @Autowired
    public S3ServiceImpl(
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey,
            @Value("${aws.s3.region}") String region,
            @Value("${aws.s3.bucket-name}") String bucketName) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        this.bucketName = bucketName;
    }

    @Override
    public String generatePresignedUrl(UUID uuid, String fileName, String contentType) {
        String key = generateKey("uploads", uuid, fileName);
        return generatePresignedUrl(key, contentType);
    }

    @Override
    public String generateVideoUploadUrl(UUID uuid, String fileName) {
        String key = generateKey("uploads", uuid, fileName);
        return generatePresignedUrl(key, "video/mp4");
    }

    @Override
    public String generateImageUploadUrl(UUID uuid, String fileName) {
        String key = generateKey("thumbnails", uuid, fileName);
        return generatePresignedUrl(key, "image/jpeg");
    }

    private String generatePresignedUrl(String key, String contentType) {
        try {
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder -> builder
                    .signatureDuration(Duration.ofMillis(EXPIRATION_MILI))
                    .putObjectRequest(por -> por
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)));
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key: {}", key, e);
            throw new CustomS3Exception("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    private String generateKey(String folder, UUID uuid, String fileName) {
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return folder + "/" + sanitized.substring(0, sanitized.lastIndexOf(".")) + "_" + uuid + sanitized.substring(sanitized.lastIndexOf("."));
    }
}