package com.courage.streamer.common.dto;

import com.courage.streamer.common.enums.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoDto {
    private UUID videoId;
    private String title;
    private String description;
    private String thumbnailKey;
    private VideoStatus status;
    private Long userId;
    private String username;
    private Instant createdAt;
}
