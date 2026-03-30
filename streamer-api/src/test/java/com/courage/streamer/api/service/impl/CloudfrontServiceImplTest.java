package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.dto.StreamResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CloudfrontServiceImplTest {

    private CloudfrontServiceImpl cloudfrontService;

    @BeforeEach
    void setUp() {
        cloudfrontService = new CloudfrontServiceImpl();
        ReflectionTestUtils.setField(cloudfrontService, "domain", "https://cdn.example.com");
        ReflectionTestUtils.setField(cloudfrontService, "keyPairId", "KEYPAIRID");
        ReflectionTestUtils.setField(cloudfrontService, "privateKeyPath", "/tmp/test.pem");
    }

    @Test
    void buildStreamResponseBuildsCorrectUrls() throws Exception {
        UUID stagingId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        StreamResponseDto response = cloudfrontService.buildStreamResponse(stagingId);

        assertNotNull(response);
        assertEquals("https://cdn.example.com/processed/" + stagingId + "/360p/index.m3u8", response.getUrl360p());
        assertEquals("https://cdn.example.com/processed/" + stagingId + "/720p/index.m3u8", response.getUrl720p());
        assertEquals("https://cdn.example.com/processed/" + stagingId + "/1080p/index.m3u8", response.getUrl1080p());
    }

    @Test
    void buildStreamResponseWithDifferentStagingIds() throws Exception {
        UUID stagingId = UUID.randomUUID();

        StreamResponseDto response = cloudfrontService.buildStreamResponse(stagingId);

        assertTrue(response.getUrl360p().contains(stagingId.toString()));
        assertTrue(response.getUrl720p().contains(stagingId.toString()));
        assertTrue(response.getUrl1080p().contains(stagingId.toString()));
    }
}
