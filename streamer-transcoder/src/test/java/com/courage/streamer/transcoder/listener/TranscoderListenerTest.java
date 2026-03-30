package com.courage.streamer.transcoder.listener;

import com.courage.streamer.transcoder.service.VideoProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TranscoderListenerTest {

    private VideoProcessor videoProcessor;
    private TranscoderListener transcoderListener;

    @BeforeEach
    void setUp() {
        videoProcessor = mock(VideoProcessor.class);
        transcoderListener = new TranscoderListener(videoProcessor);
    }

    @Test
    void receiveMessageExtractsPayloadAndReceiveCount() throws IOException {
        String payload = "{\"stagingId\": \"00000000-0000-0000-0000-000000000001\", \"s3Key\": \"uploads/video.mp4\"}";
        Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("Sqs_Msa_ApproximateReceiveCount", "1")
                .build();

        transcoderListener.receiveMessage(message);

        verify(videoProcessor).process(payload, 1);
    }

    @Test
    void receiveMessageParsesReceiveCountCorrectly() throws IOException {
        String payload = "{\"stagingId\": \"00000000-0000-0000-0000-000000000002\", \"s3Key\": \"uploads/video2.mp4\"}";
        Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("Sqs_Msa_ApproximateReceiveCount", "3")
                .build();

        transcoderListener.receiveMessage(message);

        verify(videoProcessor).process(payload, 3);
    }

    @Test
    void receiveMessagePropagatesIOException() throws IOException {
        String payload = "{}";
        Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("Sqs_Msa_ApproximateReceiveCount", "1")
                .build();

        doThrow(new IOException("processing failed")).when(videoProcessor).process(payload, 1);

        assertThrows(IOException.class, () -> transcoderListener.receiveMessage(message));
    }
}
