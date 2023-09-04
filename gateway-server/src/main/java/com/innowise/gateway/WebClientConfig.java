package com.innowise.gateway;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Autowired
    ActiveServices activeServices;

    @Bean(name = "authWebClient")
    WebClient authWebClient() {
        return WebClient.create(activeServices.authenticationApiServiceUri());
    }
}
