package com.innowise.fileapi.repo;

import com.innowise.fileapi.model.FileLocation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FileLocationRepo extends ReactiveMongoRepository<FileLocation, String> {
}
