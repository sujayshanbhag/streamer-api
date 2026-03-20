package com.courage.streamer.transcoder.listener;

import com.courage.streamer.transcoder.service.VideoProcessor;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class SQSListener {

    private final VideoProcessor videoProcessor;

    public SQSListener(VideoProcessor videoProcessor) {
        this.videoProcessor = videoProcessor;
    }

    @SqsListener("Transcoder")
    public void receiveMessage(String message) {
        try {
            System.out.println("Received message: " + message);
            videoProcessor.process(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process SQS message", e);
        }
    }
}
