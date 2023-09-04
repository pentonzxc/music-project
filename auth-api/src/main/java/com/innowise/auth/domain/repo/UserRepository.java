package com.innowise.auth.domain.repo;

import com.innowise.auth.domain.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends MongoRepository<User, Integer> {
    Optional<User> findUserByUsername(String username);
}
