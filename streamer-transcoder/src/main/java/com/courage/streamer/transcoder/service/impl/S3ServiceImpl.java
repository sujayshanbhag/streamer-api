package com.courage.streamer.transcoder.service.impl;

import com.courage.streamer.common.exception.CustomS3Exception;
import com.courage.streamer.transcoder.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    public S3ServiceImpl(S3Client s3Client,
            @Value("${spring.cloud.aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void downloadFile(String key, String downloadPath) {
        Path path = Paths.get(downloadPath);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.getObject(getObjectRequest, path);
        System.out.println("File downloaded to: " + downloadPath);

    }

    @Override
    public void uploadFile(File file, String key) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(resolveContentType(file.getName()))
                    .build();

            s3Client.putObject(putRequest, file.toPath());
            System.out.println("Uploaded: "+ key);

        } catch (Exception e) {
            throw new CustomS3Exception("Failed to upload file: " + key, e);
        }
    }

    private String resolveContentType(String fileName) {
        if (fileName.endsWith(".m3u8")) return "application/vnd.apple.mpegurl";
        if (fileName.endsWith(".ts"))   return "video/mp2t";
        return "application/octet-stream";
    }

}
