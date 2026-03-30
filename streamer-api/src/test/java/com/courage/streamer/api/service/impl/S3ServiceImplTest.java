package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.context.UserContext;
import com.courage.streamer.api.exception.CustomS3Exception;
import com.courage.streamer.api.exception.FileException;
import com.courage.streamer.common.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import java.util.UUID;
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

    private MockedStatic<UserContext> mockedUserContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        s3Service = new S3ServiceImpl(s3Client, s3Presigner, "bucket-name");

        // Mock UserContext to return a valid User
        User mockUser = mock(User.class);
        when(mockUser.getEmail()).thenReturn("test@example.com");

        // Use the class-level mockedUserContext
        mockedUserContext = mockStatic(UserContext.class);
        mockedUserContext.when(UserContext::getCurrentUser).thenReturn(mockUser);
    }

    @AfterEach
    void tearDown() {
        if (mockedUserContext != null) {
            mockedUserContext.close();
        }
    }

    @Test
    void testGeneratePresignedUrl_Success() throws MalformedURLException, URISyntaxException {
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url()).thenReturn(new URI("http://example.com").toURL());

        when(s3Presigner.presignPutObject(
                (Consumer<PutObjectPresignRequest.Builder>) any(Consumer.class)))
                .thenReturn(presignedRequest);

        String url = s3Service.generatePresignedUrl(UUID.randomUUID(), "test.mp4", "video/mp4");

        assertEquals("http://example.com", url);
    }

    @Test
    void testGeneratePresignedUrl_Exception() {
        // Arrange
        doThrow(new RuntimeException("Presign error"))
                .when(s3Presigner).presignPutObject(
                        (Consumer<PutObjectPresignRequest.Builder>) any(Consumer.class));
        // Act & Assert
        CustomS3Exception exception = assertThrows(CustomS3Exception.class, () -> s3Service.generatePresignedUrl(UUID.randomUUID(), "test.mp4", "video/mp4"  ));
        assertTrue(exception.getMessage().contains("Failed to generate presigned URL"));
    }
}
