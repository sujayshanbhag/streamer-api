package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.context.UserContext;
import com.courage.streamer.api.dto.UploadRequestDto;
import com.courage.streamer.api.dto.UploadResponseDto;
import com.courage.streamer.api.dto.VideoPageResponse;
import com.courage.streamer.api.service.S3Service;
import com.courage.streamer.api.service.VideoService;
import com.courage.streamer.common.entity.User;
import com.courage.streamer.common.entity.Video;
import com.courage.streamer.common.entity.VideoStaging;
import com.courage.streamer.common.enums.VideoStatus;
import com.courage.streamer.common.repository.VideoRepository;
import com.courage.streamer.common.repository.VideoStagingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeParseException;
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
        String signedUrl = s3Service.generatePresignedUrl(stagingId, fileName);

        VideoStaging staging = new VideoStaging();
        staging.setId(stagingId);
        staging.setCreatedBy(user.getId());
        staging.setTitle(request.getTitle());
        staging.setDescription(request.getDescription());
        staging.setOriginalFileName(fileName);

        videoStagingRepository.save(staging);

        return new UploadResponseDto(stagingId, signedUrl);
    }

    public VideoPageResponse getUserVideos(Long userId, String cursorStr, int size) {
        Instant cursor = cursorStr != null ? Instant.parse(cursorStr) : null;
        List<Video> videos = videoRepository.findByUserIdWithCursor(userId, cursor, size);
        return buildPageResponse(videos, size);
    }

    public VideoPageResponse getLiveVideos(String cursorStr, int size) {
        Instant cursor = cursorStr != null ? Instant.parse(cursorStr) : null;
        List<Video> videos = videoRepository.findLiveWithCursor(cursor, size);
        return buildPageResponse(videos, size);
    }

    private VideoPageResponse buildPageResponse(List<Video> results, int size) {
        boolean hasNextPage = results.size() > size;
        List<Video> pageItems = hasNextPage ? results.subList(0, size) : results;

        String nextCursor = hasNextPage
                ? pageItems.getLast().getCreatedAt().toString()
                : null;

        return new VideoPageResponse(pageItems, nextCursor, hasNextPage);
    }
}
