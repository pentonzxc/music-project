package com.innowise.songapi.config;

import org.apache.camel.CamelContext;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveCamel {


    @Autowired
    CamelContext context;

    @Bean
    CamelReactiveStreamsService camelReactiveStreamsService(){
        return CamelReactiveStreams.get(context);
    }
}
