package com.innowise.fileapi.config;


import com.innowise.fileapi.util.AwsConstants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;

import java.net.URI;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SqsConfig {

    final AwsCredentialsProvider credentialsProvider;

    final ConfigurableEnvironment env;

    final AwsConstants awsConstants;

    @Value("${aws.config.endpoint}")
    private String endpoint;

    @Value("${aws.config.region}")
    private String region;


    @Bean
    SqsClient sqsClient() {

        SqsClient client = SqsClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .build();

        createQueueIfNotExists(client, awsConstants.fileQueueName());
        addQueueToEnv(client, awsConstants.fileQueueName());

        return client;
    }

    private void addQueueToEnv(SqsClient client, String queueName) {
        var getQueueUrl = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();

        String queueUrl = client.getQueueUrl(getQueueUrl).queueUrl();


        MutablePropertySources properties = env.getPropertySources();

        properties.addFirst(new MapPropertySource(
                RandomStringUtils.random(10),
                Map.of(AwsConstants.queueUrl(queueUrl), queueUrl)
        ));

    }

    private void createQueueIfNotExists(SqsClient client, String queueName) {
        var getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();

        try {
            client.getQueueUrl(getQueueRequest);
        } catch (QueueDoesNotExistException __) {
            doCreateQueue(client, queueName);
        }
    }

    private void doCreateQueue(SqsClient client, String queueName) {
        client.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
    }


//    void createRequirementQueue(SqsClient client) {
//        var fileQueueUrl = "";
//
//        var getFileQueueUrl = GetQueueUrlRequest.builder()
//                .queueName("file-queue")
//                .build();
//
//        try {
//            fileQueueUrl = client.getQueueUrl(getFileQueueUrl).queueUrl();
//        } catch (Exception e) {
//            fileQueueUrl = createQueue.apply(client).queueUrl();
//        }
//
//        MutablePropertySources properties = env.getPropertySources();
//
//        properties.addFirst(new MapPropertySource("aws.sqs.runtime", Map.of("file-queue-url", fileQueueUrl)));
//    }

}
