package com.innowise.fileapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.fileapi.model.FileLocation;
import com.innowise.fileapi.model.FileLocation.Location;
import com.innowise.fileapi.repo.FileLocationRepo;
import com.innowise.fileapi.util.ExtConstants;
import com.innowise.fileapi.util.FileHelper;
import com.innowise.localstorage.LocalStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.innowise.fileapi.util.ExtConstants.JSON;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    final FileLocationRepo fileLocationRepo;

    final S3Client s3Client;

    final LocalStorage localStorage;

    final ObjectMapper objectMapper;

    @Value("${aws.s3.bucket.file}")
    private String FileBucket;


    public Mono<FileLocation> uploadAudio(String name, byte[] content, String contentExt) {
        return canAccessBucketNow()
                .publishOn(Schedulers.boundedElastic())
                .flatMap(canAccessBucket -> {

                    Map<String, String> metadata = metadata(
                            Tuples.of("name", name),
                            Tuples.of("content-type", "audio/mpeg")
                    );

                    return upload(name, content, contentExt, metadata, canAccessBucket).map(tuple2 -> {
                        String key = tuple2.getT1();
                        Location loc = tuple2.getT2();
                        FileLocation location = new FileLocation();
                        location.setLocation(loc);
                        location.setKey(key);

                        return location;
                    });

                })
                .flatMap(this::addLocationToDatabaseStorage);
    }

    private Mono<Tuple2<String, Location>> upload(String name,
                                                  byte[] content,
                                                  String contentExt,
                                                  Map<String, String> metadata,
                                                  boolean canAccessBucket) {
        log.info("Can access bucket now - {}", canAccessBucket);

        if (canAccessBucket) {
            log.info("Upload file to S3 bucket");
            return Mono.defer(() -> uploadToBucket(FileHelper.nameWithExtension(name, contentExt), content, metadata)).zipWith(Mono.just(Location.S3));
        } else {
            log.info("Upload file to local storage");
            return Mono.defer(() -> uploadToLocalStorage(name, content, contentExt, metadata)).zipWith(Mono.just(Location.LOCAL));
        }
    }


    private Mono<FileLocation> addLocationToDatabaseStorage(FileLocation location) {
        log.info("File location - {}", location);

        return fileLocationRepo.save(location);
    }


    private Mono<String> uploadToBucket(String key, byte[] content, Map<String, String> metadata) {
        /*
         transform metadata
        * */
        Supplier<Mono<Map<String, String>>> transformedMeta = () -> Mono.just(Map.of(
                "Content-Type", metadata.get("content-type"),
                "x-amz-meta-name", metadata.get("name")
        ));


        return Mono.defer(transformedMeta)
                .map(_metadata -> PutObjectRequest.builder()
                        .bucket(FileBucket)
                        .key(key)
                        .metadata(_metadata)
                        .build())
                .map(_metadata -> {
                    s3Client.putObject(_metadata, RequestBody.fromBytes(content));
                    return key;
                });
    }

    private Mono<String> uploadToLocalStorage(String name,
                                              byte[] content,
                                              String contentExt,
                                              Map<String, String> metadata) {
        return Mono.just(metadata)
                .flatMap((__) -> {
                    String metadataAsJson = null;
                    try {
                        metadataAsJson = objectMapper.writeValueAsString(metadata);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                    return localStorage.put(name, content, metadataAsJson, contentExt, JSON, true);
                })
                .map(files -> files[0]);
    }


    private Mono<Boolean> canAccessBucketNow() {
        Supplier<Mono<HeadBucketRequest>> bucketRequest = () -> Mono.just(HeadBucketRequest.builder()
                .bucket(FileBucket)
                .build());

        return Mono.defer(bucketRequest)
                .map(req -> {
                    s3Client.headBucket(req);
                    return true;
                })
                .onErrorReturn(false);

    }


    private Map<String, String> metadata(Tuple2<String, String>... param) {
        Map<String, String> map = new HashMap<>();

        for (var p : param) {
            map.put(p.getT1(), p.getT2());
        }

        return map;
    }
}

