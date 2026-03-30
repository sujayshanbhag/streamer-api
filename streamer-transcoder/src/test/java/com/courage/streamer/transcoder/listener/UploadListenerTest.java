package com.courage.streamer.transcoder.listener;

import com.courage.streamer.transcoder.service.UploadEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

class UploadListenerTest {

    private UploadEventService uploadEventService;
    private UploadListener uploadListener;

    @BeforeEach
    void setUp() {
        uploadEventService = mock(UploadEventService.class);
        uploadListener = new UploadListener(uploadEventService);
    }

    @Test
    void receiveMessageDelegatesToUploadEventService() throws IOException {
        String message = "{\"Records\": [{\"s3\": {\"object\": {\"key\": \"uploads/video.mp4\"}}}]}";

        uploadListener.receiveMessage(message);

        verify(uploadEventService).handleUpload(message);
    }

    @Test
    void receiveMessageSkipsS3TestEvents() throws IOException {
        String testEvent = "{\"Event\": \"s3:TestEvent\"}";

        uploadListener.receiveMessage(testEvent);

        verifyNoInteractions(uploadEventService);
    }

    @Test
    void receiveMessagePropagatesIOException() throws IOException {
        String message = "{\"Records\": []}";
        doThrow(new IOException("parse error")).when(uploadEventService).handleUpload(message);

        org.junit.jupiter.api.Assertions.assertThrows(IOException.class,
                () -> uploadListener.receiveMessage(message));
    }
}
