package com.innowise.fileapi.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.function.BiFunction;

@Configuration
public class AwsCredentialsConfig {

    @Value("${aws.config.accessKey}")
    private String accessKey;

    @Value("${aws.config.secretKey}")
    private String secretKey;

    private BiFunction<String, String, AwsCredentialsProvider> credentials = (access, secret) -> () -> new software.amazon.awssdk.auth.credentials.AwsCredentials() {
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
    software.amazon.awssdk.auth.credentials.AwsCredentialsProvider awsCredentialsProvider() {
        return credentials.apply(accessKey, secretKey);
    }


}
