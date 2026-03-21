package com.courage.streamer.transcoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.courage.streamer")
@EntityScan(basePackages = "com.courage.streamer")
@EnableJpaRepositories(basePackages = "com.courage.streamer")
public class VideoTranscoderApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(VideoTranscoderApplication.class, args);
    }
}
