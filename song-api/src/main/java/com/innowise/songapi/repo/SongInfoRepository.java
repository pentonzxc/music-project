package com.innowise.songapi.repo;

import com.innowise.songapi.model.SongInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SongInfoRepository extends ReactiveMongoRepository<SongInfo , Integer> {
}
