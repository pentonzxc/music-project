package com.innowise.enricher.handler;

import com.innowise.enricher.model.FileHolder;
import com.innowise.enricher.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetrieveFromSpotify implements Handler<FileHolder, Tuple2<String, String>> {

    final SpotifyService spotifyService;

    @Override
    public Flux<Tuple2<String, String>> handle(Flux<FileHolder> flux) {
        return flux.flatMap((fileHolder -> {
            String songName = fileHolder.getMetadata().get("name");

            return spotifyService.getSong(songName).zipWith(Mono.just(fileHolder.getId()));
//                    .zipWith(Mono.just(fileHolder.id)).doOnError(error -> {
//                        log.info("Something went wrong - {}", error.getMessage());
//                        log.info("Put location id back - {}", fileHolder.id);
//
//                        template.asyncSendBody(String.format("aws2-sqs:%s?queueUrl=%s&region=%s", "file-queue", fileQueueUrl, "us-west-1"), fileHolder.id);
//                    });
        }));
    }
}
