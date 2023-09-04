package com.innowise.fileapi.bean;

import com.innowise.fileapi.model.FileLocation;
import com.innowise.fileapi.util.AwsConstants;
import com.innowise.fileapi.util.CamelUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
@DependsOn("sqsConfig")
public class SQSRoute extends RouteBuilder {

    final AwsConstants awsConstants;

    @Value("${aws.config.region}")
    String Region;

    Function<Object, Object> mapToFileLocationId = (Object location) -> ((FileLocation) location).getId();


    @Override
    public void configure(){
        String fileQueueUrl = awsConstants.fileQueueUrl();
        String fileQueueName = awsConstants.fileQueueName();

        from("direct:sqs-push")
                .setBody()
                .body(mapToFileLocationId)
                .to(CamelUtils.sqsEndpoint(fileQueueName, fileQueueUrl, Region));
    }
}
