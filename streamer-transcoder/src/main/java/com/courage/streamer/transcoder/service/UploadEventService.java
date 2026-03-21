package com.courage.streamer.transcoder.service;

import java.io.IOException;

public interface UploadEventService {
    void handleUpload(String message) throws IOException;
}

