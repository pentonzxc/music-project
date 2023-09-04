package com.innowise.enricher.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.enricher.model.SongInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
@Slf4j
public class SendSongInfoToQueue implements Handler<SongInfo, Void> {

    final ObjectMapper objectMapper;

    final ProducerTemplate producer;

    final Environment env;


    @Override
    public Flux<Void> handle(Flux<SongInfo> flux) {
        String songQueueUrl = env.getProperty("song-queue-url");

        return flux.flatMap((songInfo -> {
            try {
                String message = objectMapper.writeValueAsString(songInfo);

                producer.asyncSendBody(String.format("aws2-sqs:%s?queueUrl=%s&region=%s", "song-queue", songQueueUrl, "us-west-1"), message);

            } catch (JsonProcessingException e) {
                return Mono.error(e);
            }

            return Mono.empty();
        }));
    }
}
