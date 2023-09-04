package com.innowise.enricher.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.innowise.enricher.handler.*;
import com.innowise.enricher.service.StorageService;
import com.innowise.enricher.service.SpotifyService;
import com.innowise.enricher.model.SongInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.enricher.repo.FileLocationRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@DependsOn(value = {"sqsClient"})
@RequiredArgsConstructor
public class SQSConsumer {

    final CamelReactiveStreamsService rsCamel;

    final Environment env;

    final ParseSpotifyMessage parseSpotifyMessage;

    final RetrieveFromDatabase retrieveFromDatabase;

    final RetrieveFromSpotify retrieveFromSpotify;

    final RetrieveFromStorage retrieveFromStorage;

    final VerifySpotifyToken verifySpotifyToken;

    final SendSongInfoToQueue sendSongInfoToQueue;

    @PostConstruct
    void configure() {
        String fileQueueUrl = env.getProperty("file-queue-url");
        String songQueueUrl = env.getProperty("song-queue-url");


        Publisher<String> sqsPublisher = rsCamel.from(String.format("aws2-sqs:%s?queueUrl=%s&region=%s", "file-queue", fileQueueUrl, "us-west-1"), String.class);


//        TODO: replace this hell on filter-chain pattern...

        sendSongInfoToQueue.handle(
                parseSpotifyMessage.handle(
                        retrieveFromSpotify.handle(
                                verifySpotifyToken.handle(
                                        retrieveFromStorage.handle(
                                                retrieveFromDatabase.handle(
                                                        Flux.from(sqsPublisher)
                                                )
                                        )
                                )
                        )
                )
        ).subscribe();

    }


}
