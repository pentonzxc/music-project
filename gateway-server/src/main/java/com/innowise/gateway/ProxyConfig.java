package com.innowise.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

@Configuration
public class ProxyConfig {

    @Bean
    RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("file-upload-route",
                        route -> route
                                .path("/file/upload")
                                .and()
                                .method(HttpMethod.POST)
                                .filters(filter -> filter.addRequestHeader(
                                        HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE
                                ))
                                .uri("lb://file-api"))
                .route("register-user-route",
                        route -> route
                                .path("/auth/register")
                                .and()
                                .method(HttpMethod.POST)
                                .filters((filter) -> filter.stripPrefix(1))
                                .uri("lb://auth-api"))
                .route("login-user-route",
                        route -> route
                                .path("/auth/login")
                                .and()
                                .method(HttpMethod.POST)
                                .filters((filter) -> filter.stripPrefix(1))
                                .uri("lb://auth-api"))
                .route("test-route",
                        route -> route
                                .path("/file/hi")
                                .and()
                                .method(HttpMethod.POST)
                                .uri("lb://file-api"))
                .build();
    }

}

