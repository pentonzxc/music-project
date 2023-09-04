package com.innowise.fileapi.util;


import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Arrays;
import java.util.List;


@UtilityClass
@Slf4j
public class DataBufferContent {


    public static Mono<byte[]> readToBytes(Mono<Tuple2<List<DataBuffer>, Integer>> bufferToSize) {

        return bufferToSize.map(tuple -> {
                    List<DataBuffer> buffers = tuple.getT1();
                    int size = tuple.getT2();

                    byte[] bytes = new byte[size];
                    final int[] offset = {0};

                    buffers.forEach((buffer) -> {
                        int cur_buf_length = buffer.readableByteCount();
                        buffer.read(bytes, offset[0], cur_buf_length);

                        DataBufferUtils.release(buffer);
                        offset[0] += cur_buf_length;
                    });

                    log.debug("Bytes read - {}", bytes.length);

                    return bytes;
                });
    }


    public Mono<Integer> size(Flux<DataBuffer> buffers) {
        return buffers.map(DataBuffer::readableByteCount)
                .reduce(Integer::sum)
                .defaultIfEmpty(1024);
    }
}
