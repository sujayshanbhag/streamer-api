package com.courage.streamer.api.dto;

import lombok.Data;

@Data
public class UploadRequestDto {
    private String title;
    private String description;
    private String fileName;
    private String thumbnail;
}
