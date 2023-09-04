package com.innowise.songapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

import java.net.URI;
import java.util.Map;
import java.util.function.BiFunction;

@Configuration
public class SQSConfig {

    @Value("${aws.config.accessKey}")
    private String accessKey;

    @Value("${aws.config.secretKey}")
    private String secretKey;

    @Value("${aws.config.endpoint}")
    private String endpoint;

    @Value("${aws.config.region}")
    private String region;

    @Autowired
    ConfigurableEnvironment env;



    private BiFunction<String, String, AwsCredentialsProvider> credentials = (access, secret) -> () -> new AwsCredentials() {
        @Override
        public String accessKeyId() {
            return accessKey;
        }

        @Override
        public String secretAccessKey() {
            return secretKey;
        }
    };

    void queue(SqsClient client) {
        var fileQueueUrl = "";

        var getFileQueueUrl = GetQueueUrlRequest.builder()
                .queueName("song-queue")
                .build();

        fileQueueUrl = client.getQueueUrl(getFileQueueUrl).queueUrl();

        MutablePropertySources properties = env.getPropertySources();
        properties.addFirst(new MapPropertySource("aws.sqs.runtime", Map.of("song-queue-url", fileQueueUrl)));
    }


    @Bean
    SqsClient sqsClient() {
        SqsClient client = SqsClient.builder()
                .credentialsProvider(credentials.apply(accessKey, secretKey))
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .build();

        queue(client);

        return client;
    }
}
