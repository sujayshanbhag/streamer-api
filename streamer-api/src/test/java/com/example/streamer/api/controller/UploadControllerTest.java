package com.example.streamer.api.controller;

import com.example.streamer.api.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UploadControllerTest {

    private UploadController uploadController;

    @Mock
    private S3Service s3Service;

    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        uploadController = new UploadController(s3Service);
    }

    @Test
    void testUploadFile_Success() {
        // Arrange
        doNothing().when(s3Service).uploadFile(multipartFile);

        // Act
        String response = uploadController.uploadFile(multipartFile);

        // Assert
        assertEquals("File uploaded successfully!", response);
        verify(s3Service, times(1)).uploadFile(multipartFile);
    }

    @Test
    void testGeneratePresignedUrl_Success() {
        // Arrange
        String key = "test-file.txt";
        String expectedUrl = "http://example.com";
        when(s3Service.generatePresignedUrl(key)).thenReturn(expectedUrl);

        // Act
        String response = uploadController.generatePresignedUrl(key);

        // Assert
        assertEquals(expectedUrl, response);
        verify(s3Service, times(1)).generatePresignedUrl(key);
    }
}