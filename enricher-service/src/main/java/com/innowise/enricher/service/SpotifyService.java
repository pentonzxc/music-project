package com.innowise.enricher.service;


import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class SpotifyService {

    WebClient webClient;

    @Autowired
    Environment env;


    @PostConstruct
    private void constructWebClient() {
        webClient = WebClient.builder()
                .baseUrl("https://api.spotify.com/v1")
                .build();
    }

    public Mono<String> getSong(@Nonnull String name) {
        String token = env.getProperty("accessToken");

        return webClient.get()
                .uri((builder) -> builder.path("/search")
                        .queryParam("q", name)
                        .queryParam("type", "track")
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class);
    }


    public Mono<String> verifyTokenFromEnv() {
        return getSong("something");
    }

}
