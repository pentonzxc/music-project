package com.innowise.enricher.service;

import com.innowise.enricher.model.FileHolder;
import com.innowise.enricher.model.FileLocation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import java.util.function.BiFunction;


@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    final S3AsyncClient s3Client;

    final ObjectMapper objectMapper;

    @Value("${localstorage.path}")
    String localStorage;

    @Value("${localstorage.metadataExt}")
    String metadataExt;

    @Value("${localstorage.contentExt}")
    String contentExt;

    public Mono<FileHolder> find(FileLocation fileLocation) {
        BiFunction<String, FileLocation.Location, Mono<Tuple2<Path, Map<String, String>>>> pull =
                (key, location) -> {
                    log.info("Key - {} , location  - {}", key, location);

                    if (location == FileLocation.Location.S3)
                        return pullFromS3(key);
                    else if (location == FileLocation.Location.LOCAL)
                        return pullFromLocal(key);
                    return Mono.error(new RuntimeException("File location  - " + location));
                };


        return Mono.just(fileLocation)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(fileLoc -> pull.apply(fileLoc.getKey(), fileLoc.getLocation()))
                .map((tuple) -> tuple.mapT1(path -> {
                    try {
                        return Files.readAllBytes(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .map((tuple) -> {
                    byte[] content = tuple.getT1();
                    Map<String, String> metadata = tuple.getT2();
                    log.info("content length - {} , metadata - {}", content.length, metadata);
                    return new FileHolder(content, metadata, fileLocation.getId());
                });
    }


    @SneakyThrows
    private Mono<Tuple2<Path, Map<String, String>>> pullFromS3(String key) {
        log.info("Pulling form S3");
        var req = GetObjectRequest.builder()
                .bucket("file-bucket")
                .key(key)
                .build();

        var objectPath = Files.createTempFile("secret", "secret");
        Mono<Path> path = Mono.just(objectPath);

        Mono<Map<String, String>> metadata = Mono
                .fromFuture(s3Client.getObject(req, objectPath))
                .map((GetObjectResponse::metadata));

        return path.zipWith(metadata);
    }


    @SneakyThrows
    private Mono<Tuple2<Path, Map<String, String>>> pullFromLocal(String key) {
        log.info("Pulling form Local");
        String fileName = key.split("[.]")[0];
//        #FIXME: that isn't work :)
        Path contentPath = Paths.get("localstorage", key);
        Path metadataPath = Paths.get("localstorage", fileName + ".json");
        var type = new TypeReference<Map<String, String>>() {
        };
//        #FIXME: idk , switch running context or no
        return Mono.zip(
                Mono.just(contentPath),
                Mono.just(objectMapper.readValue(Files.readAllBytes(metadataPath), type))
        ).subscribeOn(Schedulers.boundedElastic());

    }


}

