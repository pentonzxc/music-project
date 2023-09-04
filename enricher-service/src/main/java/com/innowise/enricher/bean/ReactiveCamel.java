package com.innowise.enricher.bean;

import org.apache.camel.CamelContext;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ReactiveCamel {

    @Autowired
    CamelContext camelContext;

    @Bean
    CamelReactiveStreamsService camelReactiveStreamsService() {
        return CamelReactiveStreams.get(camelContext);
    }
}
