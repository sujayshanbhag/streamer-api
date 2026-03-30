package com.courage.streamer.api.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StreamResponseDto {
    private String url360p;
    private String url720p;
    private String url1080p;
}
