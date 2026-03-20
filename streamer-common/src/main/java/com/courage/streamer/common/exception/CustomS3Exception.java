package com.courage.streamer.common.exception;

public class CustomS3Exception extends RuntimeException {
    public CustomS3Exception(String message) {
        super(message);
    }

    public CustomS3Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
