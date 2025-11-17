package com.example.demo.repository;

import com.example.demo.model.task.TaskEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<TaskEntity, String> {

    List<TaskEntity> findAllByOwnerId(String ownerId);

    Optional<TaskEntity> findTaskEntityById(String id);

}