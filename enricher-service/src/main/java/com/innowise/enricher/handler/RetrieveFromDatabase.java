package com.innowise.enricher.handler;

import com.innowise.enricher.model.FileLocation;
import com.innowise.enricher.repo.FileLocationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Component
@RequiredArgsConstructor
@Slf4j
public class RetrieveFromDatabase implements Handler<String, FileLocation> {

    final FileLocationRepo fileLocationRepo;


    @Override
    public Flux<FileLocation> handle(Flux<String> ids) {
        return ids.flatMap(fileLocationRepo::findById)
                .doOnNext((fileLocation -> log.info("File location - {}", fileLocation)));
    }
}
