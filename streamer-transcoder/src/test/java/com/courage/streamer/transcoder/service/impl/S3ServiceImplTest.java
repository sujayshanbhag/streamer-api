package com.courage.streamer.transcoder.service.impl;

import com.courage.streamer.common.exception.CustomS3Exception;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class S3ServiceImplTest {

    private S3Client s3Client;
    private S3ServiceImpl s3Service;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        s3Service = new S3ServiceImpl(s3Client, "test-bucket");
    }

    @Test
    void downloadFileDelegatesToS3Client() {
        String key = "uploads/video.mp4";
        String downloadPath = System.getProperty("java.io.tmpdir") + "/video.mp4";

        s3Service.downloadFile(key, downloadPath);

        verify(s3Client).getObject(
                argThat((GetObjectRequest r) -> r.bucket().equals("test-bucket") && r.key().equals(key)),
                any(Path.class)
        );
    }

    @Test
    void uploadFileSendsM3u8WithCorrectContentType() throws IOException {
        File file = File.createTempFile("index", ".m3u8");
        file.deleteOnExit();

        s3Service.uploadFile(file, "processed/abc/360p/index.m3u8");

        verify(s3Client).putObject(
                argThat((PutObjectRequest r) ->
                        r.bucket().equals("test-bucket")
                        && r.key().equals("processed/abc/360p/index.m3u8")
                        && r.contentType().equals("application/vnd.apple.mpegurl")),
                any(Path.class)
        );
    }

    @Test
    void uploadFileSendsTsWithCorrectContentType() throws IOException {
        File file = File.createTempFile("segment_000", ".ts");
        file.deleteOnExit();

        s3Service.uploadFile(file, "processed/abc/360p/segment_000.ts");

        verify(s3Client).putObject(
                argThat((PutObjectRequest r) -> r.contentType().equals("video/mp2t")),
                any(Path.class)
        );
    }

    @Test
    void uploadFileSendsUnknownExtensionWithOctetStream() throws IOException {
        File file = File.createTempFile("data", ".bin");
        file.deleteOnExit();

        s3Service.uploadFile(file, "processed/abc/data.bin");

        verify(s3Client).putObject(
                argThat((PutObjectRequest r) -> r.contentType().equals("application/octet-stream")),
                any(Path.class)
        );
    }

    @Test
    void uploadFileThrowsCustomS3ExceptionOnFailure() throws IOException {
        File file = File.createTempFile("video", ".mp4");
        file.deleteOnExit();

        doThrow(new RuntimeException("S3 error")).when(s3Client).putObject(any(PutObjectRequest.class), any(Path.class));

        CustomS3Exception ex = assertThrows(CustomS3Exception.class,
                () -> s3Service.uploadFile(file, "processed/key.mp4"));

        assertTrue(ex.getMessage().contains("Failed to upload file"));
    }
}
