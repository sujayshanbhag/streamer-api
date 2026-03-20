package com.courage.streamer.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.courage.streamer")
@EntityScan(basePackages = "com.courage.streamer")
@EnableJpaRepositories(basePackages = "com.courage.streamer")
public class VideoStreamerApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(VideoStreamerApplication.class, args);
	}

}
