package com.courage.streamer.transcoder.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TranscoderServiceImplTest {

    private final TranscoderServiceImpl transcoderService = new TranscoderServiceImpl();

    @Test
    void transcodeThrowsIOExceptionWhenFfmpegPathIsInvalid() {
        ReflectionTestUtils.setField(transcoderService, "ffmpegPath", "/nonexistent/ffmpeg");
        ReflectionTestUtils.setField(transcoderService, "ffprobePath", "/nonexistent/ffprobe");

        assertThrows(IOException.class,
                () -> transcoderService.transcode("/tmp/input.mp4", "/tmp/output"));
    }

    @Test
    void transcodeThrowsWhenInputPathIsNull() {
        ReflectionTestUtils.setField(transcoderService, "ffmpegPath", "/nonexistent/ffmpeg");
        ReflectionTestUtils.setField(transcoderService, "ffprobePath", "/nonexistent/ffprobe");

        assertThrows(Exception.class,
                () -> transcoderService.transcode(null, "/tmp/output"));
    }
}
