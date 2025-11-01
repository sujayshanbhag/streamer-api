package com.example.demo.exception;

public class CustomS3Exception extends RuntimeException {
    public CustomS3Exception(String message) {
        super(message);
    }

    public CustomS3Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
