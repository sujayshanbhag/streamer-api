package com.courage.streamer.api.dto;

import com.courage.streamer.common.dto.VideoDto;
import com.courage.streamer.common.entity.Video;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPageResponse {
    private List<VideoDto> videos;
    private String nextCursor;
    private boolean hasNextPage;
}
