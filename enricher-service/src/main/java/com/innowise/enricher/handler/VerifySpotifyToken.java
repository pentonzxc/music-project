package com.innowise.enricher.handler;

import com.innowise.enricher.model.FileHolder;
import com.innowise.enricher.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifySpotifyToken implements Handler<FileHolder, FileHolder> {

    final SpotifyService spotifyService;

    final ProducerTemplate producer;

    final Environment env;

    @Override
    public Flux<FileHolder> handle(Flux<FileHolder> flux) {
        String fileQueueUrl = env.getProperty("file-queue-url");

        return flux.flatMap((fileHolder) ->
                spotifyService.verifyTokenFromEnv()
                        .map(ignore -> fileHolder)
                        .doOnError((error) -> {
                            log.info("Something went wrong - {}", error.getMessage());
                            log.info("Put location id back - {}", fileHolder.getId());

                            producer.asyncSendBody(String.format("aws2-sqs:%s?queueUrl=%s&region=%s", "file-queue", fileQueueUrl, "us-west-1"), fileHolder.getId());
                        })
        ).retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)));
    }
}
