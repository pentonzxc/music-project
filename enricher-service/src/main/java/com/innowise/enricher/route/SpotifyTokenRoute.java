package com.innowise.enricher.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.String.format;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SpotifyTokenRoute extends RouteBuilder {

    final ConfigurableEnvironment env;

    final ObjectMapper mapper;

    @Value("${spotify.clientId}")
    String clientId;

    @Value("${spotify.clientSecret}")
    String clientSecret;

    String requestBody;

    String spotifyCamelUri;


    @PostConstruct()
    void constructRequestBody() {
        requestBody = format(
                "grant_type=client_credentials" +
                        "&client_id=%s" +
                        "&client_secret=%s", clientId, clientSecret
        );

        spotifyCamelUri = "rest:post:https://accounts.spotify.com/api/token?" +
                "consumes=application/x-www-form-urlencoded&produces=application/json";
    }


    @Override
    public void configure() throws Exception {

        restConfiguration()
                .component("netty-http")
                .host("localhost")
                .port(8084)
                .bindingMode(RestBindingMode.json);

        from("scheduler:spotify-access-token?delay=55&timeUnit=MINUTES")
                .to("direct:get-spotify-access-token");

        from("direct:get-spotify-access-token")
                .setBody().simple(requestBody)
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
                .setHeader(Exchange.CONTENT_TYPE, constant(ContentType.APPLICATION_FORM_URLENCODED))
                .to("netty-http:https://accounts.spotify.com/api/token?matchOnUriPrefix=true")
                .transform(body().convertToString())
                .log(LoggingLevel.INFO, "spotify api response - ${body}")
                .process(addAccessTokenToEnv());
    }

    public Processor addAccessTokenToEnv() {
        return exchange -> {
            String body = (String) exchange.getMessage().getBody();
            Object accessToken = mapper.readValue(body, Map.class).get("access_token");

            MutablePropertySources properties = env.getPropertySources();
            properties.addFirst(new MapPropertySource("spotify", Map.of("accessToken", accessToken)));
        };
    }

}
