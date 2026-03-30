package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.context.UserContext;
import com.courage.streamer.api.dto.UploadRequestDto;
import com.courage.streamer.api.dto.UploadResponseDto;
import com.courage.streamer.api.dto.VideoPageResponse;
import com.courage.streamer.api.service.S3Service;
import com.courage.streamer.common.dto.VideoDto;
import com.courage.streamer.common.entity.User;
import com.courage.streamer.common.entity.VideoStaging;
import com.courage.streamer.common.repository.VideoRepository;
import com.courage.streamer.common.repository.VideoStagingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VideoServiceImplTest {

    private VideoServiceImpl videoService;
    private S3Service s3Service;
    private VideoStagingRepository videoStagingRepository;
    private VideoRepository videoRepository;
    private MockedStatic<UserContext> mockedUserContext;

    @BeforeEach
    void setUp() {
        s3Service = mock(S3Service.class);
        videoStagingRepository = mock(VideoStagingRepository.class);
        videoRepository = mock(VideoRepository.class);
        videoService = new VideoServiceImpl(s3Service, videoStagingRepository, videoRepository);

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        mockedUserContext = mockStatic(UserContext.class);
        mockedUserContext.when(UserContext::getCurrentUser).thenReturn(mockUser);
    }

    @AfterEach
    void tearDown() {
        mockedUserContext.close();
    }

    @Test
    void initiateUploadWithoutThumbnailSavesAndReturnsResponse() {
        UploadRequestDto request = new UploadRequestDto();
        request.setFileName("video.mp4");
        request.setTitle("My Video");
        request.setDescription("A description");

        when(s3Service.generateVideoUploadUrl(any(UUID.class), eq("video.mp4")))
                .thenReturn("https://s3.example.com/signed-url");

        UploadResponseDto response = videoService.initiateUpload(request);

        assertNotNull(response);
        assertNotNull(response.getVideoId());
        assertEquals("https://s3.example.com/signed-url", response.getVideoSignedUrl());
        assertNull(response.getThumbnailSignedUrl());
        verify(videoStagingRepository).save(any(VideoStaging.class));
    }

    @Test
    void initiateUploadWithThumbnailGeneratesBothUrls() {
        UploadRequestDto request = new UploadRequestDto();
        request.setFileName("video.mp4");
        request.setTitle("My Video");
        request.setDescription("A description");
        request.setThumbnail("thumb.jpg");

        when(s3Service.generateVideoUploadUrl(any(UUID.class), eq("video.mp4")))
                .thenReturn("https://s3.example.com/video-url");
        when(s3Service.generateImageUploadUrl(any(UUID.class), eq("thumb.jpg")))
                .thenReturn("https://s3.example.com/thumb-url");

        UploadResponseDto response = videoService.initiateUpload(request);

        assertNotNull(response);
        assertEquals("https://s3.example.com/video-url", response.getVideoSignedUrl());
        assertEquals("https://s3.example.com/thumb-url", response.getThumbnailSignedUrl());
        verify(videoStagingRepository).save(any(VideoStaging.class));
    }

    @Test
    void getUserVideosReturnsSinglePageWhenResultsWithinSize() {
        VideoDto video = new VideoDto();
        video.setCreatedAt(Instant.now());

        when(videoRepository.findByUserIdWithCursor(eq(1L), isNull(), eq(10)))
                .thenReturn(List.of(video));

        VideoPageResponse response = videoService.getUserVideos(1L, null, 10);

        assertEquals(1, response.getVideos().size());
        assertFalse(response.isHasNextPage());
        assertNull(response.getNextCursor());
    }

    @Test
    void getUserVideosReturnsHasNextPageWhenMoreResultsThanSize() {
        Instant now = Instant.now();
        VideoDto v1 = new VideoDto();
        v1.setCreatedAt(now.minusSeconds(10));
        VideoDto v2 = new VideoDto();
        v2.setCreatedAt(now.minusSeconds(20));

        // repository returns size+1 items to signal there's a next page
        when(videoRepository.findByUserIdWithCursor(eq(1L), isNull(), eq(1)))
                .thenReturn(List.of(v1, v2));

        VideoPageResponse response = videoService.getUserVideos(1L, null, 1);

        assertEquals(1, response.getVideos().size());
        assertTrue(response.isHasNextPage());
        assertNotNull(response.getNextCursor());
    }

    @Test
    void getLiveVideosReturnsSinglePageWhenResultsWithinSize() {
        VideoDto video = new VideoDto();
        video.setCreatedAt(Instant.now());

        when(videoRepository.findLiveWithCursor(isNull(), eq(10))).thenReturn(List.of(video));

        VideoPageResponse response = videoService.getLiveVideos(null, 10);

        assertEquals(1, response.getVideos().size());
        assertFalse(response.isHasNextPage());
    }

    @Test
    void getLiveVideosParsesCursorString() {
        String cursorStr = "2024-01-01T00:00:00Z";
        Instant cursor = Instant.parse(cursorStr);

        when(videoRepository.findLiveWithCursor(eq(cursor), eq(10))).thenReturn(List.of());

        VideoPageResponse response = videoService.getLiveVideos(cursorStr, 10);

        assertNotNull(response);
        verify(videoRepository).findLiveWithCursor(cursor, 10);
    }

    @Test
    void findVideoByIdReturnsVideoDtoWhenFound() {
        UUID videoId = UUID.randomUUID();
        VideoDto dto = new VideoDto();
        dto.setVideoId(videoId);

        when(videoRepository.findVideoById(videoId)).thenReturn(Optional.of(dto));

        VideoDto result = videoService.findVideoById(videoId);

        assertEquals(videoId, result.getVideoId());
    }

    @Test
    void findVideoByIdThrowsWhenNotFound() {
        UUID videoId = UUID.randomUUID();
        when(videoRepository.findVideoById(videoId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> videoService.findVideoById(videoId));

        assertTrue(exception.getMessage().contains("Video not found"));
    }
}
