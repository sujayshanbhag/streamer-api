package com.courage.streamer.transcoder.service;

import java.io.File;

public interface S3Service {
    void downloadFile(String key, String downloadPath);
    void uploadFile(File file, String key);
}
