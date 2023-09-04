package com.innowise.fileapi.util;


import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AwsConstants {

    final Environment env;

    public static String queueUrl(String queueName) {
        return queueName.concat("-").concat("url");
    }

    public String fileQueueName() {
        return env.getProperty("aws.sqs.queue.file");
    }

    public String fileQueueUrl() {
        return env.getProperty(queueUrl(fileQueueName()));
    }

//    TODO: all work with constants and century place here
}
