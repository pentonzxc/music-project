package com.innowise.fileapi.web;


import com.innowise.fileapi.model.FileLocation;
import com.innowise.fileapi.service.FileUploadService;
import com.innowise.fileapi.util.DataBufferContent;
import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

import static com.innowise.fileapi.util.ExtConstants.MP3;


@RestController
@RequestMapping("/file-api")
@RequiredArgsConstructor
public class FileController {

    final FileUploadService uploadService;

    final ProducerTemplate producer;


    @RequestMapping(
            value = "/download",
            method = RequestMethod.GET,
            produces = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    Mono<?> download(String fileId) {

    }

    @PreAuthorize("hasAuthority('permission:write')")
    @RequestMapping(
            value = "/upload",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    Mono<FileLocation> upload(@RequestPart(name = "name") String fileName, @RequestPart("content") FilePart fileContent) {
        Flux<DataBuffer> buffers = fileContent.content();

        Mono<Tuple2<List<DataBuffer>, Integer>> buffersToLength = buffers
                .collectList()
                .zipWith(DataBufferContent.size(buffers));

        return DataBufferContent.readToBytes(buffersToLength)
                .flatMap(bytes -> uploadService.uploadAudio(fileName, bytes, MP3))
                .doOnNext(this::pushMessageToQueue);
    }


    void pushMessageToQueue(FileLocation fileLocation) {
        producer.asyncSendBody("direct:sqs-push", fileLocation);
    }
}
