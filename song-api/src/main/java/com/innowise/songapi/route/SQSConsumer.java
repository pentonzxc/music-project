package com.innowise.songapi.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.songapi.model.SongInfo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@DependsOn("sqsClient")
@RequiredArgsConstructor
public class SQSConsumer {

    final SqsClient sqsClient;

    final CamelReactiveStreamsService camelReactiveStreamsService;

    final Environment env;

    final ObjectMapper objectMapper;


    @PostConstruct
    void init() {
        String fileQueueUrl = env.getProperty("song-queue-url");


        Flux.from(camelReactiveStreamsService.from(
                        String.format("aws2-sqs:%s?queueUrl=%s&region=%s", "song-queue", fileQueueUrl, "us-west-1"), String.class)
                )
                .map(message -> {
                    try {
                        System.out.println(message);
                        return objectMapper.readValue(message, SongInfo.class);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                })
                .subscribe(System.out::println);
    }

}
