package com.courage.streamer.transcoder.service;

import java.io.IOException;

public interface TranscoderService {
    public void transcode(String inputPath, String outputDir) throws IOException;
}
