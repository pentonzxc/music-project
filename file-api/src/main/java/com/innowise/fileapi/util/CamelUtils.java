package com.innowise.fileapi.util;


import lombok.experimental.UtilityClass;

@UtilityClass
public class CamelUtils {

    public static String sqsEndpoint(String queueName , String queueUrl, String queueRegion) {
        return String.format("aws2-sqs://%s?queueUrl=%s&region=%s" , queueName, queueUrl, queueRegion);
    }

}
