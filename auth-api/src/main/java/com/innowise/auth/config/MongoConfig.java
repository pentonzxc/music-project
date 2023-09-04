package com.innowise.auth.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@EnableReactiveMongoRepositories
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create();
    }


    @Override
    protected String getDatabaseName() {
        return "user-storage";
    }
}
