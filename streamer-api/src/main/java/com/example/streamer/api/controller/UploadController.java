package com.example.streamer.api.controller;

import com.example.streamer.api.service.S3Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    private final S3Service s3Service;

    UploadController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        s3Service.uploadFile(file);
        return "File uploaded successfully!";
    }

    @GetMapping("/signed-url/{key}")
    public String generatePresignedUrl(@PathVariable("key") String key) {
        return s3Service.generatePresignedUrl(key);
    }
}
