package com.courage.streamer.transcoder.listener;

import com.courage.streamer.transcoder.service.UploadEventService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UploadListener {

    private final UploadEventService uploadEventService;

    public UploadListener(UploadEventService uploadEventService) {
        this.uploadEventService = uploadEventService;
    }

    @SqsListener(value = "upload", factory = "uploadQueueFactory")
    public void receiveMessage(String message) throws IOException {
        if (message.contains("s3:TestEvent")) return;
        uploadEventService.handleUpload(message);
    }
}

