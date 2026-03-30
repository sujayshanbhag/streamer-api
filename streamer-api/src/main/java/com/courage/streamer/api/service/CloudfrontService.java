package com.courage.streamer.api.service;

import com.courage.streamer.api.dto.StreamResponseDto;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;

import java.util.UUID;

public interface CloudfrontService {
    CookiesForCustomPolicy getSignedCookies(UUID stagingId) throws Exception;
    StreamResponseDto buildStreamResponse(UUID stagingId) throws Exception;
}
