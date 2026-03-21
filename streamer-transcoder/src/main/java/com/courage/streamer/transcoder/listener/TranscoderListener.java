package com.courage.streamer.transcoder.listener;

import com.courage.streamer.transcoder.service.VideoProcessor;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TranscoderListener {

    private final VideoProcessor videoProcessor;

    public TranscoderListener(VideoProcessor videoProcessor) {
        this.videoProcessor = videoProcessor;
    }

    @SqsListener(value = "transcoder", factory = "transcoderQueueFactory")
    public void receiveMessage(Message<String> message) throws IOException {
        String payload = message.getPayload();

        String receiveCountStr = message.getHeaders()
                .get("Sqs_Msa_ApproximateReceiveCount", String.class);
        int receiveCount = Integer.parseInt(receiveCountStr);

        videoProcessor.process(payload, receiveCount);
    }
}
