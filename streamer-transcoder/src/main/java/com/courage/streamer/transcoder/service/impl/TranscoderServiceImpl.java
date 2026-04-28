package com.courage.streamer.transcoder.service.impl;

import com.courage.streamer.transcoder.service.TranscoderService;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class TranscoderServiceImpl implements TranscoderService {

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${ffprobe.path}")
    private String ffprobePath;

    private static final Map<String, String> RESOLUTIONS = Map.of(
            "360p",  "640:360",
            "720p",  "1280:720",
            "1080p", "1920:1080"
    );

    @Override
    public void transcode(String inputPath, String outputDir) throws IOException {
        FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
        FFprobe ffprobe = new FFprobe(ffprobePath);
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        for (Map.Entry<String, String> resolution : RESOLUTIONS.entrySet()) {
            String label = resolution.getKey();
            String scale = resolution.getValue();
            String resolutionOutputDir = outputDir + "/" + label;

            // Create output directory
            new File(resolutionOutputDir).mkdirs();

            FFmpegBuilder builder = new FFmpegBuilder()
                    .addExtraArgs("-threads", "1")         // 1 decoder thread
                    .setInput(inputPath)
                    .overrideOutputFiles(true)
                    .addOutput(resolutionOutputDir + "/index.m3u8")
                    .setFormat("hls")
                    .addExtraArgs("-hls_time", "10")
                    .addExtraArgs("-hls_list_size", "0")
                    .addExtraArgs("-hls_segment_filename", resolutionOutputDir + "/segment_%03d.ts")
                    .addExtraArgs("-vf", "scale=" + scale)
                    .addExtraArgs("-threads", "1")          // 1 encoder thread
                    .addExtraArgs("-x264-params", "threads=1") // hard-cap libx264
                    .done();

            executor.createJob(builder).run();

            System.out.println("Transcoded " + label + " successfully");
        }
    }
}
