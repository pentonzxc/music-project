package com.innowise.enricher.config;

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
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.net.URI;
import java.util.Map;
import java.util.function.BiFunction;

@Configuration
public class SqsConfig {

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

    private BiFunction<SqsClient, String, GetQueueUrlResponse> createQueue = (SqsClient client, String queueName) -> {
        var create_queue = CreateQueueRequest.builder().queueName(queueName).build();
        var queue_url = "";

        queue_url = client.createQueue(create_queue).queueUrl();

        return GetQueueUrlResponse.builder().queueUrl(queue_url).build();
    };


    void queue(SqsClient client) {
        var fileQueueUrl = "";
        var songQueueUrl = "";

        var getFileQueueUrl = GetQueueUrlRequest.builder()
                .queueName("file-queue")
                .build();
        var cl = client.getQueueUrl(getFileQueueUrl);
        fileQueueUrl = cl.queueUrl();

        var getSongQueueUrl = GetQueueUrlRequest.builder()
                .queueName("song-queue")
                .build();
        try {
            songQueueUrl = client.getQueueUrl(getSongQueueUrl).queueUrl();
        } catch (Exception e) {
            songQueueUrl = createQueue.apply(client, getSongQueueUrl.queueName()).queueUrl();
        }

        MutablePropertySources properties = env.getPropertySources();

        properties.addFirst(new MapPropertySource("aws.sqs.1", Map.of("file-queue-url", fileQueueUrl)));
        properties.addFirst(new MapPropertySource("aws.sqs.2", Map.of("song-queue-url", songQueueUrl)));
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


    @Bean
    SqsAsyncClient sqsAsyncClient() {
        SqsAsyncClient client = SqsAsyncClient.builder()
                .credentialsProvider(credentials.apply(accessKey, secretKey))
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .build();

        return client;
    }

}
