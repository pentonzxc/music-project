package com.innowise.enricher.repo;

import com.innowise.enricher.model.FileLocation;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository(value = "fileRepo")
public interface FileLocationRepo extends ReactiveMongoRepository<FileLocation, String> {

    @Override
    Mono<FileLocation> findById(String id);

    @Override
    Mono<FileLocation> findById(Publisher<String> id);
}
