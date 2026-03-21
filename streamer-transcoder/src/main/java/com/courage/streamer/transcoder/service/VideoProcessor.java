package com.courage.streamer.transcoder.service;

import java.io.IOException;

public interface VideoProcessor {
    void process(String message, int retryCount) throws IOException;
}
