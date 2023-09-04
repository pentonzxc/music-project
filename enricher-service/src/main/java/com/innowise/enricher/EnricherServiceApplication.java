package com.innowise.enricher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.innowise.localstorage")
public class EnricherServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnricherServiceApplication.class, args);
    }

}
