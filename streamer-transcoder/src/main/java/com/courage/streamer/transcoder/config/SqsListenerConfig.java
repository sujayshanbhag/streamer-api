package com.courage.streamer.transcoder.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.util.Collections;

@Configuration
public class SqsListenerConfig {
    @Bean
    public SqsMessageListenerContainerFactory<Object> transcoderQueueFactory(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory
                .builder()
                .configure(options -> options
                        .maxConcurrentMessages(1)
                        .maxMessagesPerPoll(1)
                        .messageAttributeNames(Collections.singletonList("All"))
                )
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> uploadQueueFactory(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory
                .builder()
                .configure(options -> options
                        .maxConcurrentMessages(10)
                        .maxMessagesPerPoll(10)
                )
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }

}
