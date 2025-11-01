package com.example.demo.service;


import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface S3Service {
    void uploadFile(MultipartFile file);
    String generatePresignedUrl(String fileName);
}
