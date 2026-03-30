package com.courage.streamer.api.service.impl;

import com.courage.streamer.api.dto.StreamResponseDto;
import com.courage.streamer.api.service.CloudfrontService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Service
public class CloudfrontServiceImpl implements CloudfrontService {

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

    @Value("${cloudfront.domain}")
    private String domain;

    @Value("${cloudfront.key-pair-id}")
    private String keyPairId;

    @Value("${cloudfront.private-key-path}")
    private String privateKeyPath;

    public CookiesForCustomPolicy getSignedCookies(UUID videoId) throws Exception {

        CustomSignerRequest request = CustomSignerRequest.builder()
                .resourceUrl(domain + "/processed/" + videoId + "/*")
                .privateKey(Path.of(privateKeyPath))
                .keyPairId(keyPairId)
                .expirationDate(Instant.now().plusSeconds(60 * 60))
                .build();

        return cloudFrontUtilities.getCookiesForCustomPolicy(request);
    }

    public StreamResponseDto buildStreamResponse(UUID stagingId) throws Exception {
        String base = domain + "/processed/" + stagingId;
        return StreamResponseDto.builder()
                .url360p(base + "/360p/index.m3u8")
                .url720p(base + "/720p/index.m3u8")
                .url1080p(base + "/1080p/index.m3u8")
                .build();
    }
}
