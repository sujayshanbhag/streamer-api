package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.exception.CustomS3Exception;
import com.courage.streamer.api.exception.FileException;
import com.courage.streamer.api.service.impl.S3ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class S3ServiceImplTest {

    private S3ServiceImpl s3Service;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        s3Service = new S3ServiceImpl(s3Client, s3Presigner, "bucket-name");
    }

    @Test
    void testUploadFile_Success() throws IOException {
        // Arrange
        File tempFile = Files.createTempFile("test-file", ".txt").toFile();
        when(multipartFile.getOriginalFilename()).thenReturn("test-file.txt");
        doAnswer(invocation -> {
            File file = invocation.getArgument(0);
            Files.write(file.toPath(), "test content".getBytes());
            return null;
        }).when(multipartFile).transferTo(any(File.class));

        // Act & Assert
        assertDoesNotThrow(() -> s3Service.uploadFile(multipartFile));
        tempFile.deleteOnExit();
    }

    @Test
    void testUploadFile_IOException() throws IOException {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("test-file.txt");
        doThrow(new IOException("File transfer failed")).when(multipartFile).transferTo(any(File.class));

        // Act & Assert
        FileException exception = assertThrows(FileException.class, () -> s3Service.uploadFile(multipartFile));
        assertEquals("Failed to upload file to S3: File transfer failed", exception.getMessage());
    }

    @Test
    void testUploadFile_S3Exception() throws IOException {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("test-file.txt");
        doNothing().when(multipartFile).transferTo(any(File.class));
        doThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder()
                .message("S3 error")
                .build())
                .when(s3Client).putObject(any(PutObjectRequest.class), any(java.nio.file.Path.class));

        // Act & Assert
        CustomS3Exception exception = assertThrows(CustomS3Exception.class, () -> s3Service.uploadFile(multipartFile));
        assertTrue(exception.getMessage().contains("Failed to upload file to S3"));
    }

    @Test
    void testGeneratePresignedUrl_Success() throws MalformedURLException, URISyntaxException {
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url()).thenReturn(new URI("http://example.com").toURL());

        when(s3Presigner.presignPutObject(
                (Consumer<PutObjectPresignRequest.Builder>) any(Consumer.class)))
                .thenReturn(presignedRequest);

        String url = s3Service.generatePresignedUrl("test-file.txt");

        assertEquals("http://example.com", url);
    }



    @Test
    void testGeneratePresignedUrl_Exception() {
        // Arrange
        doThrow(new RuntimeException("Presign error"))
                .when(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));
        // Act & Assert
        CustomS3Exception exception = assertThrows(CustomS3Exception.class, () -> s3Service.generatePresignedUrl("test-file.txt"));
        assertTrue(exception.getMessage().contains("Failed to generate presigned URL"));
    }
}
