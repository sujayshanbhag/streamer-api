package com.courage.streamer.transcoder.service.impl;

import com.courage.streamer.transcoder.service.S3Service;
import com.courage.streamer.transcoder.service.TranscoderService;
import com.courage.streamer.transcoder.service.VideoProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@Service
public class VideoProcessorImpl implements VideoProcessor {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private TranscoderService transcoderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void process(String message) throws IOException {
        if (message.contains("s3:TestEvent")) return;

        JsonNode root = objectMapper.readTree(message);
        JsonNode records = root.path("Records");
        if (records.isMissingNode() || records.isEmpty()) return;

        String rawKey = records.path(0).path("s3").path("object").path("key").asText();
        String key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);

        String fileName = key.substring(key.lastIndexOf("/") + 1);
        String fileNameWithoutExt = fileName.replace(".mp4", "");
        
        String tempDir = System.getProperty("java.io.tmpdir");

        String inputPath = tempDir + File.separator + "input" + File.separator + fileName;
        String outputPath = tempDir + File.separator + "output" + File.separator + fileNameWithoutExt;


        File downloadedFile = new File(inputPath);
        File outputPathFile = new File(outputPath);


        try {
            downloadedFile.getParentFile().mkdirs();
            outputPathFile.mkdirs();

            // 1. Download
            s3Service.downloadFile(key, inputPath);

            // 2. Transcode
            transcoderService.transcode(inputPath, outputPath);

            // 3. Upload
            String s3OutputPrefix = "processed/" + fileNameWithoutExt;
            uploadDirectory(outputPathFile, outputPath, s3OutputPrefix);

            System.out.println("Transcoding and upload completed for: " + key);

        } finally {
            deleteDirectory(outputPathFile);
            downloadedFile.delete();
        }
    }


    private void uploadDirectory(File directory, String localBaseDir, String s3Prefix) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                uploadDirectory(file, localBaseDir, s3Prefix);
            } else {
                // preserve relative path: 360p/index.m3u8, 360p/segment_000.ts etc
                String relativePath = file.getAbsolutePath()
                        .substring(localBaseDir.length())
                        .replace("\\", "/")  // Windows path fix
                        .replaceFirst("^/", "");
                String s3Key = s3Prefix + "/" + relativePath;
                s3Service.uploadFile(file, s3Key);
            }
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        Path dirPath = directory.toPath();
        if (!Files.exists(dirPath)) return;

        Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                boolean deleted = file.toFile().delete();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                boolean deleted = dir.toFile().delete();
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
