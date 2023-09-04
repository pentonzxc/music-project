package com.innowise.enricher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.util.function.BiFunction;

@Configuration
public class S3Config {

    @Value("${aws.config.accessKey}")
    private String accessKey;

    @Value("${aws.config.secretKey}")
    private String secretKey;

    @Value("${aws.config.endpoint}")
    private String endpoint;


    @Value("${aws.config.region}")
    private String region;


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


    @Bean
    S3AsyncClient s3AsyncClient() {
        return S3AsyncClient.builder()
                .credentialsProvider(credentials.apply(accessKey, secretKey))
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    S3Client s3Client() {
        return S3Client.builder()
                .credentialsProvider(credentials.apply(accessKey, secretKey))
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
                .build();
    }


}
