package com.innowise.fileapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.fileapi.model.FileLocation;
import com.innowise.fileapi.repo.FileLocationRepo;
import com.innowise.localstorage.LocalStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadService {
    final FileLocationRepo fileLocationRepo;

    final S3Client s3Client;

    final LocalStorage localStorage;

    @Value()
    final String fileBucket;


    public Mono<?> download(String fileId) {
        Mono<FileLocation> fileLocation = fileLocationRepo.findById(fileId);
        return doDownload(fileLocation);
    }

    private Mono<?> doDownload(Mono<FileLocation> fileLocation){



        return fileLocation.doOnNext((loc) -> log.info("download file - {}" , loc))
                .
    }


    private Mono<?> downloadFromS3(String key){
        s3Client.getObject(GetObjectRequest.builder()
                .key(key)
                .bucket().build())
    }

    private Mono<?> downloadFromLocalStorage(String key) {

    }



}
