package com.innowise.fileapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.List;


@Configuration
@EnableWebFlux
public class CorsPolicy implements WebFluxConfigurer {

    private List<String> Origins = List.of("*");

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowedOrigins(Origins.toArray(new String[0]))
                .maxAge(3600L);
    }
}
