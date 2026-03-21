package com.courage.streamer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponseDto {
    private UUID videoId;
    private String uploadUrl;
}
