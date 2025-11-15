package com.example.demo.repository;

import com.example.demo.model.user.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findUserEntityById(String id);

}