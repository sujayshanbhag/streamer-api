package com.courage.streamer.transcoder.model;

import com.courage.streamer.common.enums.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscoderMessage {
    Long userId;
    private UUID stagingId;
    private String title;
    private String description;
    private String s3Key;
    private String originalFileName;
}
