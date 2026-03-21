package com.courage.streamer.api.dto;

import lombok.Data;

@Data
public class VideoRequestDto {
    private String title;
    private String description;
    private String fileName;
}
