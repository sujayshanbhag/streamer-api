package com.courage.streamer.api.controller;


import com.courage.streamer.api.dto.AccountPageDto;
import com.courage.streamer.api.dto.StreamResponseDto;
import com.courage.streamer.common.dto.VideoDto;
import com.courage.streamer.api.dto.VideoPageResponse;
import com.courage.streamer.api.service.CloudfrontService;
import com.courage.streamer.api.service.VideoService;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;

import java.util.UUID;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoService videoService;
    private final CloudfrontService cloudfrontService;

    @Value("${app.domain}")
    private  String domain;

    public VideoController(VideoService videoService, CloudfrontService cloudfrontService) {
        this.videoService = videoService;
        this.cloudfrontService = cloudfrontService;
    }

    @GetMapping
    public ResponseEntity<VideoPageResponse> getLiveVideos(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        return ResponseEntity.ok(videoService.getLiveVideos(keyword, cursor, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<AccountPageDto> getUserVideos(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        return ResponseEntity.ok(videoService.getUserVideos(keyword, userId, cursor, size));
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<StreamResponseDto> getSignedCookies(
            @PathVariable("videoId") UUID videoId,
            HttpServletResponse response) throws Exception {
        StreamResponseDto streamResponse = cloudfrontService.buildStreamResponse(videoId);
        CookiesForCustomPolicy cookies = cloudfrontService.getSignedCookies(videoId);

        addCookie(response, cookies.policyHeaderValue());
        addCookie(response, cookies.signatureHeaderValue());
        addCookie(response, cookies.keyPairIdHeaderValue());

        return ResponseEntity.ok(streamResponse);
    }

    @GetMapping("/{videoId}/details")
    public ResponseEntity<VideoDto> getVideoDetails(
            @PathVariable("videoId") UUID videoId
    ) {
        VideoDto videoDetails = videoService.findVideoById(videoId);
        return ResponseEntity.ok(videoDetails);
    }

    private void addCookie(HttpServletResponse response, String headerValue) {
        String cookieString = headerValue + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=3600";

        if (domain != null && !domain.isBlank() && !domain.equals("localhost")) {
            cookieString += "; Domain=" + domain + "; Secure";
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieString);
    }
}

