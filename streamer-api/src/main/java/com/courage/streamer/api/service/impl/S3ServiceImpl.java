package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.exception.CustomS3Exception;
import com.courage.streamer.api.exception.FileException;
import com.courage.streamer.api.service.S3Service;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

@AllArgsConstructor
@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    private final String bucketName;

    private final static long EXPIRATION_MILI = 900_000; // 15 minutes


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
    public void uploadFile(MultipartFile file) {
        String key = generateKey(file.getOriginalFilename());
        File tempFile = null;
        try {
            // Create a temporary file
            tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            // Transfer the contents of MultipartFile to the temporary file
            file.transferTo(tempFile);

            // Create a PutObjectRequest for S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // Upload the file to S3
            s3Client.putObject(putObjectRequest, tempFile.toPath());
        } catch (IOException e) {
            throw new FileException("Failed to upload file to S3: " + e.getMessage(), e);
        } catch (S3Exception e) {
            throw new CustomS3Exception("Failed to upload file to S3: " + e.getMessage(), e);
        }catch (SdkException e) {
            throw new CustomS3Exception("AWS SDK error occurred: " + e.getMessage(), e);
        } finally {
            // Ensure the temporary file is deleted
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("Failed to delete temporary file: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public String generatePresignedUrl(String fileName) {
        String key = generateKey(fileName);
        try {
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder -> builder
                    .signatureDuration(Duration.ofMillis(EXPIRATION_MILI))
                    .putObjectRequest(por -> por.bucket(bucketName).key(key)));
            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new CustomS3Exception("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    private String generateKey(String fileName) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss"));
        return fileName + "-" + timestamp;
    }
}
