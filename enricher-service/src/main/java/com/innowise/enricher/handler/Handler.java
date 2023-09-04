package com.innowise.enricher.handler;

import reactor.core.publisher.Flux;

public interface Handler<Input, Output> {
    Flux<Output> handle(Flux<Input> flux);
}
