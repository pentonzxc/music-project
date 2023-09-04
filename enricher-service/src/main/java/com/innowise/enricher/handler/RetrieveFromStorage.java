package com.innowise.enricher.handler;

import com.innowise.enricher.model.FileHolder;
import com.innowise.enricher.model.FileLocation;
import com.innowise.enricher.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Component
@RequiredArgsConstructor
@Slf4j
public class RetrieveFromStorage implements Handler<FileLocation, FileHolder> {

    final StorageService storageService;


    @Override
    public Flux<FileHolder> handle(Flux<FileLocation> flux) {
        return flux.flatMap(storageService::find)
                .doOnNext((fileHolder -> log.info("File holder - {}", fileHolder)));
    }
}
