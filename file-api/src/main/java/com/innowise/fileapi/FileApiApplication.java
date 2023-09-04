package com.innowise.fileapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.innowise.fileapi.model.FileLocation;
import org.apache.camel.CamelContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;

@SpringBootApplication
public class FileApiApplication {

    public static void main(String[] args) {
		SpringApplication.run(FileApiApplication.class, args);
    }

}
