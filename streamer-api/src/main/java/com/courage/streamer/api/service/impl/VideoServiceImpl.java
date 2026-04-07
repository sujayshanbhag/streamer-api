package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.context.UserContext;
import com.courage.streamer.api.dto.*;
import com.courage.streamer.api.service.S3Service;
import com.courage.streamer.api.service.VideoService;
import com.courage.streamer.common.dto.VideoDto;
import com.courage.streamer.common.entity.User;
import com.courage.streamer.common.entity.VideoStaging;
import com.courage.streamer.common.repository.VideoRepository;
import com.courage.streamer.common.repository.VideoStagingRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class VideoServiceImpl  implements VideoService {

    private final S3Service s3Service;
    private final VideoStagingRepository videoStagingRepository;
    private final VideoRepository videoRepository;

    public VideoServiceImpl(S3Service s3Service, VideoStagingRepository videoStagingRepository, VideoRepository videoRepository) {
        this.s3Service = s3Service;
        this.videoStagingRepository = videoStagingRepository;
        this.videoRepository = videoRepository;
    }

    @Override
    public UploadResponseDto initiateUpload(UploadRequestDto request) {
        User user = UserContext.getCurrentUser();
        String fileName = request.getFileName();
        UUID stagingId = UUID.randomUUID();
        String signedUrl = s3Service.generateVideoUploadUrl(stagingId, fileName);

        String thumbnailSignedUrl = null;
        String thumbnailKey = null;
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            thumbnailSignedUrl = s3Service.generateImageUploadUrl(stagingId, request.getThumbnail());
            thumbnailKey = "thumbnails/" + request.getThumbnail().substring(0, request.getThumbnail().lastIndexOf('.'))
                    + "_" + stagingId + request.getThumbnail().substring(request.getThumbnail().lastIndexOf('.'));
        }

        VideoStaging staging = new VideoStaging();
        staging.setId(stagingId);
        staging.setCreatedBy(user.getId());
        staging.setTitle(request.getTitle());
        staging.setDescription(request.getDescription());
        staging.setThumbnailKey(thumbnailKey);

        videoStagingRepository.save(staging);

        return new UploadResponseDto(stagingId, signedUrl, thumbnailSignedUrl);
    }

    @Override
    public AccountPageDto getUserVideos(String key, Long userId, String cursorStr, int size) {
        Instant cursor = cursorStr != null ? Instant.parse(cursorStr) : null;
        List<VideoDto> videos;
        if(key != null && !key.isEmpty()) {
            videos = videoRepository.findByUserIdAndKeyWithCursor(userId, key, cursor, Pageable.ofSize(size + 1));
        } else {
            videos = videoRepository.findLiveByUserIdWithCursor(userId, cursor, Pageable.ofSize(size + 1));
        }
        Long count = videoRepository.countByCreatedBy(userId);
        return new AccountPageDto(count, buildPageResponse(videos, size));
    }

    public VideoPageResponse getLiveVideos(String keyword, String cursorStr, int size) {
        List<VideoDto> videos;
        Instant cursor = cursorStr != null ? Instant.parse(cursorStr) : null;
        if(keyword != null && !keyword.isEmpty()) {
            videos = videoRepository.findLiveByTitleOrDescription(keyword, cursor, Pageable.ofSize(size + 1));

        } else {
            videos = videoRepository.findLiveWithCursor(cursor, Pageable.ofSize(size + 1));

        }
        return buildPageResponse(videos, size);
    }

    private VideoPageResponse buildPageResponse(List<VideoDto> results, int size) {
        boolean hasNextPage = results.size() > size;
        List<VideoDto> pageItems = hasNextPage ? results.subList(0, size) : results;

        String nextCursor = hasNextPage
                ? pageItems.getLast().getCreatedAt().toString()
                : null;

        return new VideoPageResponse(pageItems, nextCursor, hasNextPage);
    }

    @Override
    public VideoDto findVideoById(UUID videoId) {
        return videoRepository.findVideoById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + videoId));
    }
}
