package com.courage.streamer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountPageDto {
    Long totalVideos;
    VideoPageResponse videos;
}
