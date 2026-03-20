package com.courage.streamer.transcoder.service;

import java.io.IOException;

public interface VideoProcessor {
    public void process(String message) throws IOException;
}
