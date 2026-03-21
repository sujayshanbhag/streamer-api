package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.context.UserContext;
import com.courage.streamer.api.dto.VideoRequestDto;
import com.courage.streamer.api.dto.VideoResponseDto;
import com.courage.streamer.api.service.S3Service;
import com.courage.streamer.api.service.VideoService;
import com.courage.streamer.common.entity.User;
import com.courage.streamer.common.entity.VideoStaging;
import com.courage.streamer.common.repository.VideoStagingRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VideoServiceImpl  implements VideoService {

    private final S3Service s3Service;
    private final VideoStagingRepository videoStagingRepository;

    public VideoServiceImpl(S3Service s3Service, VideoStagingRepository videoStagingRepository) {
        this.s3Service = s3Service;
        this.videoStagingRepository = videoStagingRepository;
    }

    @Override
    public VideoResponseDto initiateUpload(VideoRequestDto request) {
        User user = UserContext.getCurrentUser();
        String fileName = request.getFileName();
        UUID stagingId = UUID.randomUUID();
        String signedUrl = s3Service.generatePresignedUrl(stagingId, fileName);

        VideoStaging staging = new VideoStaging();
        staging.setId(stagingId);
        staging.setUserId(user.getId());
        staging.setTitle(request.getTitle());
        staging.setDescription(request.getDescription());
        staging.setOriginalFileName(fileName);

        videoStagingRepository.save(staging);

        return new VideoResponseDto(stagingId, signedUrl);
    }
}
